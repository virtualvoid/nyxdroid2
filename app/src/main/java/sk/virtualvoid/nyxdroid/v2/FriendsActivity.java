package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.am.FriendsActionMode;
import sk.virtualvoid.nyxdroid.v2.data.Friend;
import sk.virtualvoid.nyxdroid.v2.data.adapters.FriendAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.FriendDataAccess;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * 
 * @author Juraj
 * 
 */
public class FriendsActivity extends BaseActivity {
	private GetFriendsTaskListener getFriendsTaskListener = new GetFriendsTaskListener();
	private Task<ITaskQuery, ArrayList<Friend>> tempTask;
	private ActionMode actionModeHandle;

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
				final Friend friend = (Friend) parent.getItemAtPosition(position);

				FriendsActionMode actionMode = new FriendsActionMode(FriendsActivity.this, new FriendsActionMode.Listener() {
					@Override
					public void onSendMail() {
						actionModeHandle.finish();

						Intent intent = new Intent(FriendsActivity.this, MailComposeActivity.class);
						intent.putExtra(Constants.KEY_NICK, friend.Nick);
						startActivityForResult(intent, 0);
						overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
					}

					@Override
					public void onGotoDiscussion() {
						actionModeHandle.finish();

						Long discussionId = friend.Location.discussion();
						if (discussionId != null) {
							Intent intent = new Intent(FriendsActivity.this, WriteupsActivity.class);
							intent.putExtra(Constants.KEY_ID, discussionId);
							startActivity(intent);
							overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
							finish();
						}
					}
				});

				actionModeHandle = startActionMode(actionMode);
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
		getMenuInflater().inflate(R.menu.friends_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh:
				return refresh();
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean refresh() {
		TaskManager.killIfNeeded(tempTask);

		tempTask = FriendDataAccess.getFriends(FriendsActivity.this, getFriendsTaskListener);
		TaskManager.startTask(tempTask, ITaskQuery.empty);

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
	private static class GetFriendsTaskListener extends TaskListener<ArrayList<Friend>> {
		@Override
		public void done(ArrayList<Friend> output) {
			FriendsActivity context = (FriendsActivity) getContext();
			context.setListAdapter(new FriendAdapter(context, output));
			
			context.getPullToRefreshAttacher().setRefreshComplete();
		}
	}
}
