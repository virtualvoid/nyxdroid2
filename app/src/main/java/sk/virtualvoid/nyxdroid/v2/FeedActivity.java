package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.v2.data.Feed;
import sk.virtualvoid.nyxdroid.v2.data.FeedComment;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.FeedAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.FeedDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.FeedQuery;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class FeedActivity extends BaseActivity {
	private FeedsTaskListener feedsTaskListener = new FeedsTaskListener();
	private PostFeedTaskListener postFeedTaskListener = new PostFeedTaskListener();
	private DetailFeedTaskListener detailFeedTaskListener = new DetailFeedTaskListener();

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
			public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
				Feed feed = (Feed) parent.getItemAtPosition(position);
				detail(feed);
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
		getMenuInflater().inflate(R.menu.feed_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.compose) {
			compose();
			return true;
		}
		if (item.getItemId() == R.id.refresh) {
			refresh();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refresh() {		
		Task<FeedQuery, ArrayList<Feed>> task = FeedDataAccess.getFeeds(FeedActivity.this, feedsTaskListener);
		
		FeedQuery query = new FeedQuery();
		TaskManager.startTask(task, query);
	}

	private void compose() {
		final Task<FeedQuery, NullResponse> task = FeedDataAccess.postFeed(FeedActivity.this, postFeedTaskListener);
		
		FeedComposeDialog dialog = new FeedComposeDialog(FeedActivity.this, new FeedComposeDialog.OnPostListener() {
			@Override
			public void onPost(String value) {
				FeedQuery query = new FeedQuery();
				query.Message = value;
				TaskManager.startTask(task, query);
			}
		});

		dialog.show();
	}
	
	private void detail(Feed feed) {
		FeedQuery query = new FeedQuery();
		query.Id = feed.Id;
		query.Nick = feed.Nick;
		
		Task<FeedQuery, ArrayList<FeedComment>> task = FeedDataAccess.getFeedDetails(FeedActivity.this, detailFeedTaskListener);
		TaskManager.startTask(task, query);
	}

	/**
	 * 
	 * @author juraj
	 * 
	 */
	private static class FeedsTaskListener extends TaskListener<ArrayList<Feed>> {
		@Override
		public void done(ArrayList<Feed> output) {
			FeedActivity context = (FeedActivity) getContext();
			
			FeedAdapter feedsAdapter = new FeedAdapter(context, output);
			context.setListAdapter(feedsAdapter);
			
			context.getPullToRefreshAttacher().setRefreshComplete();
		}
	}
	
	/**
	 * 
	 * @author juraj
	 *
	 */
	private static class PostFeedTaskListener extends TaskListener<NullResponse> {
		@Override
		public void done(NullResponse output) {
			((FeedActivity)getContext()).refresh();
		}
	}

	/**
	 * 
	 * @author juraj
	 *
	 */
	private static class DetailFeedTaskListener extends TaskListener<ArrayList<FeedComment>> {
		@Override
		public void done(ArrayList<FeedComment> output) {
			FeedQuery query = (FeedQuery) getTag();
			
			FeedCommentDialog dialog = new FeedCommentDialog((Activity) getContext(), query, output);
			dialog.show();
		}
	}
}
