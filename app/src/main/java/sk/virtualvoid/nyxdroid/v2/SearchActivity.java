package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.Tuple;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Search;
import sk.virtualvoid.nyxdroid.v2.data.adapters.SearchAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.SearchDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.SearchQuery;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

/**
 * 
 * @author Juraj
 * 
 */
public class SearchActivity extends BaseActivity {
	private SearchQuery currentSearchQuery = new SearchQuery();
	private SearchTaskListener searchListener = new SearchTaskListener();
	private Task<SearchQuery, ArrayList<Search>> tempTask;
	private SearchAdapter adapter;
	private SearchView searchView;

	@Override
	protected int getContentViewId() {
		return R.layout.generic_listview;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ListView lv = getListView();
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Search search = (Search) parent.getItemAtPosition(position);

				Intent intent = new Intent(SearchActivity.this, WriteupsActivity.class);
				intent.putExtra(Constants.KEY_ID, search.DiscussionId);
				intent.putExtra(Constants.KEY_WU_ID, search.Id + 1);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_menu, menu);

		MenuItem searchMenuItem = menu.findItem(R.id.search);
		searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				if (searchView != null) {
					searchView.requestFocus();
				}
				Log.d(Constants.TAG, "SearchView on SearchActivity expanded.");
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				Log.d(Constants.TAG, "SearchView on SearchActivity closed.");
				return true;
			}
		});

		searchView = (SearchView) searchMenuItem.getActionView();
		
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

		searchView.setFocusable(true);
		searchView.setIconified(false);
		searchView.requestFocusFromTouch();
		
		return true;
	}

	private void executeSearch() {
		TaskManager.killIfNeeded(tempTask);

		tempTask = SearchDataAccess.search(SearchActivity.this, searchListener);
		TaskManager.startTask(tempTask, currentSearchQuery);
	}

	private void search(String text) {
		Tuple<String, String> tuple = CoreUtility.splitSearch(text);
		if (tuple == null) {
			Log.w(Constants.TAG, "Empty search term.");
			return;
		}

		currentSearchQuery.Position = 1;
		currentSearchQuery.Nick = tuple.First;
		currentSearchQuery.Phrase = tuple.Second;

		executeSearch();
	}

	@Override
	protected void onListViewDataRequested() {
		if (currentSearchQuery.Position++ > 49) {
			Log.w(Constants.TAG, "SearchActivity listview requested new data, but limit has been reached.");
			return;
		}

		executeSearch();
	}

	@Override
	public boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId) {
		/* Not needed here */
		return false;
	}
	
	/**
	 * 
	 * @author Juraj
	 * 
	 */
	private class SearchTaskListener extends TaskListener<ArrayList<Search>> {
		@Override
		public void done(ArrayList<Search> output) {
			SearchActivity context = (SearchActivity) getContext();

			if (currentSearchQuery.Position == 1) {
				adapter = new SearchAdapter(context, output);
				context.setListAdapter(adapter);
			} else {
				adapter.addItems(adapter.filter(output));
				adapter.notifyDataSetChanged();
			}
		}
	}
}
