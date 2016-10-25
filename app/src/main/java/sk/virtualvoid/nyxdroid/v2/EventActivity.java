package sk.virtualvoid.nyxdroid.v2;

import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Event;
import sk.virtualvoid.nyxdroid.v2.data.dac.EventDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.EventQuery;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

/**
 * 
 * @author Juraj
 * 
 */
public class EventActivity extends BaseActivity implements ActionBar.TabListener {
	public static final int TAB_DETAILS = 0x1;
	public static final int TAB_COMMENTS = 0x2;

	private Task<EventQuery, Event> tempTask;
	private GetEventDetailTaskListener listener = new GetEventDetailTaskListener();
	private long id;
	private Long commentId;

	private ImageDownloader imageDownloader;
	private ImageGetterAsync imageGetterAsync;
	private int linkColor;

	private String eventStatus;
	private String[] eventStatuses;

	private Fragment currentFragment;
	private EventDetailFragment detailFragment;
	private EventCommentsFragment commentsFragment;

	@Override
	protected boolean useSlidingMenu() {
		return false;
	}

	@Override
	protected int getContentViewId() {
		return R.layout.empty_view;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		linkColor = appearance.getLinkColor();

		Resources resources = getResources();
		
		imageDownloader = new ImageDownloader(this, resources.getDrawable(R.drawable.empty_avatar));
		imageGetterAsync = new ImageGetterAsync(this);

		eventStatuses =resources.getStringArray(R.array.event_statuses);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		commentsFragment = (EventCommentsFragment) fm.findFragmentByTag(EventCommentsFragment.TAG);
		if (commentsFragment == null) {
			commentsFragment = new EventCommentsFragment();
		}
		ft.add(R.id.empty_view_ll, commentsFragment, EventCommentsFragment.TAG);
		ft.hide(commentsFragment);

		currentFragment = detailFragment = (EventDetailFragment) fm.findFragmentByTag(EventDetailFragment.TAG);
		if (currentFragment == null) {
			currentFragment = detailFragment = new EventDetailFragment();
		}		
		ft.add(R.id.empty_view_ll, detailFragment, EventDetailFragment.TAG);
		ft.show(detailFragment);

		ft.commit();

		actionBar.addTab(actionBar.newTab().setText(R.string.event_details).setTag(TAB_DETAILS).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.event_comments).setTag(TAB_COMMENTS).setTabListener(this));

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		id = extras.getLong(Constants.KEY_ID);
		commentId = extras.getLong(Constants.KEY_COMMENT_ID);
	
		refresh();
	}

	private void setData(Event event) {
		setTitle(event.Title);

		eventStatus = event.Status;
	}

	private void refresh() {
		EventQuery query = new EventQuery();
		query.Id = id;

		TaskManager.killIfNeeded(tempTask);

		tempTask = EventDataAccess.getEventDetail(EventActivity.this, listener);
		TaskManager.startTask(tempTask, query);
	}

	private void changeStatus() {
		int checkedItem = -1;
		
		final String[] eventStatusesAPI = new String[] { Constants.EVENT_API_STATUS_ATTENDING, Constants.EVENT_API_STATUS_INTERESTED, Constants.EVENT_API_STATUS_NONE };
		for (int i = 0; i < eventStatusesAPI.length; i++) {
			if (eventStatus.equals(eventStatusesAPI[i])) {
				checkedItem = i;
				break;
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.choose_one_from_options);
		builder.setSingleChoiceItems(eventStatuses, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ListView lv = ((AlertDialog) dialog).getListView();
				lv.setTag(which);
			}
		});

		builder.setPositiveButton(R.string.event_choose_status, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ListView lv = ((AlertDialog) dialog).getListView();
				if (!(lv.getTag() instanceof Integer)) {
					return;
				}
				int choice = (Integer) lv.getTag();

				EventQuery query = new EventQuery();
				query.Id = id;
				query.ChangeStatus = eventStatus = eventStatusesAPI[choice];

				Task<EventQuery, Void> task = EventDataAccess.changeStatus(EventActivity.this, TaskListener.EmptyListener);
				TaskManager.startTask(task, query);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.event_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}

		if (item.getItemId() == R.id.change_status) {
			changeStatus();
		}

		return true;
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (currentFragment != null) {
			ft.hide(currentFragment);
		}

		if (TAB_DETAILS == (Integer) tab.getTag()) {
			currentFragment = detailFragment;
		}

		if (TAB_COMMENTS == (Integer) tab.getTag()) {
			currentFragment = commentsFragment;
		}

		ft.show(currentFragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	public ImageDownloader getImageDownloader() {
		return imageDownloader;
	}

	public int getLinkColor() {
		return linkColor;
	}

	public ImageGetterAsync getImageGetter() {
		return imageGetterAsync;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	public interface EventFragmentHandler {
		void setData(Event data, Object tag);
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	private class GetEventDetailTaskListener extends TaskListener<Event> {
		@Override
		public void done(Event event) {
			setData(event);

			detailFragment.setData(event, null);
			commentsFragment.setData(event, commentId);
		}
	}
}
