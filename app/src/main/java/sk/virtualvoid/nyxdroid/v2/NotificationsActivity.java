package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Notice;
import sk.virtualvoid.nyxdroid.v2.data.NoticeType;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.NoticeAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.NoticeDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.NoticeQuery;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * @author Juraj
 * 
 */
public class NotificationsActivity extends BaseActivity {
	private GetNoticesTaskListener listener = new GetNoticesTaskListener();
	private Task<NoticeQuery, SuccessResponse<ArrayList<Notice>>> tempTask;

	private boolean refreshReceiverEnabled;

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
				Notice notice = (Notice) parent.getItemAtPosition(position);

				if (notice.Type == NoticeType.NOTICE || notice.Type == NoticeType.REPLY) {
					if (notice.Section.equalsIgnoreCase(Notice.SECTION_TOPICS)) {
						Intent intent = new Intent(NotificationsActivity.this, WriteupsActivity.class);
						intent.putExtra(Constants.KEY_ID, notice.DiscussionId);
						intent.putExtra(Constants.KEY_WU_ID, notice.WriteupId + 1);
						startActivity(intent);
						overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
					}
				}
				
				if (notice.Type == NoticeType.THUMBS) {
					WriteupRatingsDialog dialog = new WriteupRatingsDialog(NotificationsActivity.this, notice.DiscussionId, notice.WriteupId);
					dialog.show();
				}
			}
		});

		getPullToRefreshAttacher().setRefreshableView(lv, new PullToRefreshAttacher.OnRefreshListener() {
			@Override
			public void onRefreshStarted(View view) {
				refresh();
			}
		});

		refreshReceiverEnabled = true;
		registerReceiver(refreshReceiver, new IntentFilter(Constants.REFRESH_NOTICES_INTENT_FILTER));

		refresh();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshReceiver);
	}

	@Override
	protected void onPause() {
		super.onPause();
		refreshReceiverEnabled = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshReceiverEnabled = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.notices_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.refresh) {
			return refresh();
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean refresh() {
		TaskManager.killIfNeeded(tempTask);

		NoticeQuery query = new NoticeQuery();
		query.KeepNew = false;

		tempTask = NoticeDataAccess.getNotifications(this, listener);
		TaskManager.startTask(tempTask, query);

		getPullToRefreshAttacher().setRefreshing(true);

		return true;
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
	private static class GetNoticesTaskListener extends TaskListener<SuccessResponse<ArrayList<Notice>>> {
		@Override
		public void done(SuccessResponse<ArrayList<Notice>> output) {
			NotificationsActivity context = (NotificationsActivity) getContext();

			NoticeAdapter adapter = new NoticeAdapter(context, output.getData());
			context.setListAdapter(adapter);
			
			context.getPullToRefreshAttacher().setRefreshComplete();
			context.displayMailNotificationOnToolbar(output.getContext());
			//context.displayReplyNotificationOnToolbar(); akshually, this is not needed here
		}
	}

	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!refreshReceiverEnabled) {
				Log.w(Constants.TAG, "refreshReceiver not enabled.");
				return;
			}

			refresh();
		}
	};
}
