package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;
import java.util.HashMap;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Bookmark;
import sk.virtualvoid.nyxdroid.v2.data.BookmarkCategory;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.BookmarkAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.BookmarkDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.BookmarkQuery;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;


/**
 * 
 * @author Juraj
 * 
 */
public class BookmarksActivity extends BaseActivity {
	private boolean movementMode;
	private boolean unreadBookmarks;
	private boolean unreadHistory;
	private HashMap<Long, Boolean> expandState;
	private Task<BookmarkQuery, SuccessResponse<ArrayList<Bookmark>>> tempTask = null;
	private BookmarkAdapter adapter;
	private final BookmarksTaskListener bookmarksTaskListener = new BookmarksTaskListener();
	private SearchView searchView;

	@Override
	protected int getContentViewId() {
		return R.layout.generic_listview;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		movementMode = extras.getBoolean(Constants.KEY_BOOKMARKS_IS_HISTORY);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		unreadBookmarks = prefs.getBoolean("unread_bookmarks", true);
		unreadHistory = prefs.getBoolean("unread_bookmarks_history", true);

		expandState = new HashMap<Long, Boolean>();

		// TODO: v api je teraz EXPAND parameter, takze niekedy v buducnosti toto rozklikavanie kategorii treba optimalizovat
		ListView lv = getListView();
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bookmark bookmark = (Bookmark) parent.getItemAtPosition(position);
				if (bookmark instanceof BookmarkCategory) {
					TaskManager.killIfNeeded(tempTask);

					expandState.put(bookmark.Id, expandState.containsKey(bookmark.Id) ? !expandState.get(bookmark.Id) : unreadBookmarks);

					BookmarkQuery query = new BookmarkQuery();
					query.CategoryId = bookmark.Id;

					tempTask = BookmarkDataAccess.getInCategory(BookmarksActivity.this, bookmarksTaskListener);
					TaskManager.startTask(tempTask, query);
				} else {
					bookmark.markRead();

					notifyDatasetChanged();

					Intent intent = new Intent(BookmarksActivity.this, WriteupsActivity.class);
					intent.putExtra(Constants.KEY_ID, id);
					intent.putExtra(Constants.KEY_TITLE, bookmark.Name);
					startActivity(intent);
					overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				}
			}
		});

		getPullToRefreshAttacher().setRefreshableView(lv, new PullToRefreshAttacher.OnRefreshListener() {
			@Override
			public void onRefreshStarted(View view) {
				refresh();
			}
		});

		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.bookmarks_menu, menu);

		MenuItem searchMenuItem = menu.findItem(R.id.search);
		searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				if (searchView != null) {
					searchView.requestFocus();
				}
				Log.d(Constants.TAG, "SearchView on BookmarksActivity expanded.");
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				refresh();
				Log.d(Constants.TAG, "SearchView on BookmarksActivity closed.");
				return true;
			}
		});

		searchView = (SearchView) searchMenuItem.getActionView();
		searchView.setFocusable(true);
		
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				search(query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});

		return onAfterCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh:
				return refresh();
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean search(String searchTerm) {
		BookmarkQuery query = new BookmarkQuery();
		query.SearchTerm = searchTerm;

		TaskManager.killIfNeeded(tempTask);

		tempTask = BookmarkDataAccess.searchBookmarks(BookmarksActivity.this, bookmarksTaskListener);
		TaskManager.startTask(tempTask, query);

		return true;
	}

	private boolean refresh() {
		BookmarkQuery query = new BookmarkQuery();

		TaskManager.killIfNeeded(tempTask);

		if (movementMode) {
			setTitle(R.string.app_name_bookmarks_history);

			query.IncludeUnread = unreadHistory;

			tempTask = BookmarkDataAccess.getMovement(BookmarksActivity.this, bookmarksTaskListener);
			TaskManager.startTask(tempTask, query);
		} else {
			setTitle(R.string.app_name_bookmarks);

			query.IncludeUnread = unreadBookmarks;

			tempTask = BookmarkDataAccess.getBookmarks(BookmarksActivity.this, bookmarksTaskListener);
			TaskManager.startTask(tempTask, query);
		}

		getPullToRefreshAttacher().setRefreshing(true);

		return true;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	private static class BookmarksTaskListener extends TaskListener<SuccessResponse<ArrayList<Bookmark>>> {
		@Override
		public void done(SuccessResponse<ArrayList<Bookmark>> output) {
			BookmarksActivity activity = (BookmarksActivity) getContext();
			BookmarkQuery query = (BookmarkQuery) getTag();

			if (query.CategoryId == null) {
				activity.expandState.clear();
				activity.setListAdapter(activity.adapter = new BookmarkAdapter(activity, output.getData()));
			} else {
				boolean expand = activity.expandState.get(query.CategoryId);
				if (expand) {
					activity.adapter.clearCategory(query.CategoryId);
				} else {
					activity.adapter.replaceCategory(query.CategoryId, output.getData());
				}
				activity.adapter.notifyDataSetChanged();
			}
			
			activity.getPullToRefreshAttacher().setRefreshComplete();

			activity.displayMailNotificationOnToolbar(output.getContext());
			activity.displayReplyNotificationOnToolbar(output.getContext());
		}
	}
}
