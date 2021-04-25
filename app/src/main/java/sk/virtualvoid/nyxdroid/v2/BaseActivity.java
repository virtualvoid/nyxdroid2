package sk.virtualvoid.nyxdroid.v2;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.log4j.Logger;

import pub.devrel.easypermissions.EasyPermissions;
import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.ITaskKey;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.ResponsibleBaseAdapter;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.net.nyx.IConnectorReporter;
import sk.virtualvoid.net.nyx.IConnectorReporterHandler;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Context;
import sk.virtualvoid.nyxdroid.v2.internal.Appearance;
import sk.virtualvoid.nyxdroid.v2.internal.INavigationHandler;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationHandler;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gcm.GCMRegistrar;

/**
 * 
 * @author juraj
 * 
 */
public abstract class BaseActivity extends AppCompatActivity implements IConnectorReporterHandler, INavigationHandler, ITaskKey, EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {
	private final static Logger log = Logger.getLogger(BaseActivity.class);
	private static volatile int ACTIVITY_COUNT = 0;

	protected Appearance appearance;

	private Menu toolbarMenu;

	private BaseMenu baseMenu;
	private ListView listView;

	private PullToRefreshAttacher pull;

	private int currentOrientation;

	private int scrollFirstVisibleItem;
	private int scrollVisibleItemCount;
	private int scrollTotalItemCount;

	// ===================================================================================

	protected boolean useSlidingMenu() {
		return true;
	}

	protected PullToRefreshAttacher getPullToRefreshAttacher() {
		if (pull == null) {
			pull = new PullToRefreshAttacher(this);
		}
		return pull;
	}

	protected abstract int getContentViewId();

	protected void onListViewDataRequested() {
	}

	protected void onListViewScrollUp() {
	}

	protected void onListViewScrollDown() {
	}

	// ===================================================================================

	public String getTaskKey() {
		return getClass().getCanonicalName();
	}

	// ===================================================================================

	public ResponsibleBaseAdapter getResponsibleBaseAdapter() {
		return null;
	}

	private synchronized void setupResponsibleBaseAdapter(boolean uiActive) {
		if (getResponsibleBaseAdapter() == null) {
			return;
		}

		ResponsibleBaseAdapter responsibleBaseAdapter = getResponsibleBaseAdapter();
		ImageGetterAsync imageGetterAsync = responsibleBaseAdapter.getImageGetterAsync();

		imageGetterAsync.setActive(!uiActive);
	}

	// ===================================================================================

	public void handleConnectorReporter(IConnectorReporter connectorReporter) {
		if (connectorReporter == null) {
			return;
		}

		if (!connectorReporter.error()) {
			return;
		}

		log.error(String.format("handleConnectorReporter: status = %d, description %s", connectorReporter.status(), connectorReporter.description()));
		for (String header : connectorReporter.headers()) {
			log.debug(String.format("handleConnectorReporter: HEADER = %s", header));
		}

		if (connectorReporter.isAuthorizationError()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
			builder.setTitle(R.string.error);
			builder.setMessage(R.string.authorization_error_message);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					GCMRegistrar.setRegisteredOnServer(BaseActivity.this, false);
					Connector.authorizationRemove(BaseActivity.this);
					BaseActivity.this.finish();
				}
			});

			AlertDialog dialog = builder.create();
			dialog.show();
		}

		if (connectorReporter.isDataError()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
			builder.setTitle(R.string.error);
			builder.setMessage(R.string.unspecified_data_error);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	// ===================================================================================

	@Override
	public boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId) {
		// pre zachovanie historie pohybu po aktivitach, nefinishujem aktualnu

		if (navigationType == NavigationType.TOPIC) {
			NavigationHandler.startNavigateTopic(this, WriteupsActivity.class, discussionId, writeupId);
		}

//		if (navigationType == NavigationType.EVENT) {
//			NavigationHandler.startNavigateEvent(this, EventActivity.class, discussionId, null);
//		}

//		if (navigationType == NavigationType.MARKET) {
//			NavigationHandler.startNavigateMarket(this, AdvertActivity.class, discussionId);
//		}

		if (navigationType == NavigationType.IMAGE) {
			try {
				String decodedUrl = URLDecoder.decode(url, Constants.DEFAULT_CHARSET.displayName());

				if (!CoreUtility.launchBrowser(this, decodedUrl)) {
					Toast.makeText(this, R.string.cant_open_it, Toast.LENGTH_SHORT).show();
				}
			} catch (UnsupportedEncodingException e) {
				log.error("BaseActivity::onNavigationRequested: IMAGE url decoding problem: " + e.getMessage());
			}
		}

		return true;
	}

	// ===================================================================================
	protected boolean onAfterCreateOptionsMenu(Menu menu) {
		this.toolbarMenu = menu;
		return true;
	}

	// ===================================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		appearance = Appearance.getAppearance(this);

		if (appearance.getUseDarkTheme()) {
			setTheme(R.style.NyxdroidTheme);
		} else {
			setTheme(R.style.NyxdroidTheme_Light);
		}

		// mne sa vazne nechcelo srat so spravnym handlovanim zmeny P/L
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// setup view
		setContentView(getContentViewId());

		// current orientation? this is actually unused - remove ?
		currentOrientation = getRequestedOrientation();

		// hide progressbar (since he is visible by calling
		// requestWindowFeature)
		setProgressBarIndeterminateVisibility(false);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// setup action bar icon based on theme
		if (appearance.getUseDarkTheme()) {
			actionBar.setIcon(R.drawable.ic_activities_dark);
		} else {
			actionBar.setIcon(R.drawable.ic_activities);
		}

		// setup side navigation, and it's theme
		initializeMenu();

		// restore ?
		if (savedInstanceState == null) {
			Log.w(Constants.TAG, "ONCREATE NOT RESTORING");
		} else {
			Log.w(Constants.TAG, "ONCREATE RESTORING !");
		}

		// activity tracking
		ACTIVITY_COUNT++;
		Log.w(Constants.TAG, "onResume, ActivityCount: " + ACTIVITY_COUNT);
	}

	protected void initializeMenu() {
		if (useSlidingMenu() && baseMenu == null) {
			baseMenu = new BaseMenu(this);
			baseMenu.initialize();
		}
	}

	protected void initializeSecondMenu() {
		if (useSlidingMenu() && baseMenu != null) {
			baseMenu.initializeSecondMenu();
		}
	}

	protected void toggleMenu() {
		if (useSlidingMenu() && baseMenu != null) {
			baseMenu.toggle();
		}
	}

//	protected FloatingActionButton initializeFloatingActionButton(View.OnClickListener onClickListener) {
//		ImageView icon = new ImageView(this);
//
//		if (appearance.getUseDarkTheme()) {
//			icon.setImageResource(R.drawable.dark_action_add);
//		} else {
//			icon.setImageResource(R.drawable.light_action_add);
//		}
//
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		int position = Integer.parseInt(prefs.getString("sendbutton_position", Integer.toString(FloatingActionButton.POSITION_BOTTOM_RIGHT)));
//
//		FloatingActionButton actionButton = new FloatingActionButton.Builder(this).setContentView(icon).setPosition(position).setTheme(appearance.getUseDarkTheme() ? FloatingActionButton.THEME_DARK : FloatingActionButton.THEME_LIGHT).build();
//
//		actionButton.setOnClickListener(onClickListener);
//
//		return actionButton;
//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ACTIVITY_COUNT--;

		// clear cache only on 'real' app-exit
		if (ACTIVITY_COUNT <= 0) {
			ImageGetterAsync.clearDrawableCache();
		}

		Log.w(Constants.TAG, "onDestroy, ActivityCount: " + ACTIVITY_COUNT);
	}

	// ===================================================================================
	public boolean hasNavigationBar() {
		int id = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
		return id > 0 && getResources().getBoolean(id);
	}
	protected void setupListViewInstance(final ListView listViewInstance) {
		View emptyView = findViewById(R.id.list_empty);
		if (emptyView != null) {
			listViewInstance.setEmptyView(emptyView);
		}

		if (!hasNavigationBar()) {
			// 99% sure there's not a navigation bar so padding are set to 0 because of rid off a empty space
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) listViewInstance.getLayoutParams();
			params.bottomMargin = 0;
		}

		listViewInstance.setOnScrollListener(new AbsListView.OnScrollListener() {
			private int mPreviousScrollY;
			private int mPreviousFirstVisibleItem;
			public int mLastChangeY;
			private int mMinSignificantScroll;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mMinSignificantScroll = view.getContext().getResources().getDimensionPixelOffset(R.dimen.fab_min_significant_scroll);

				if (scrollState == SCROLL_STATE_IDLE) {
					setupResponsibleBaseAdapter(false);
				}

				if (scrollState == SCROLL_STATE_IDLE && scrollFirstVisibleItem + scrollVisibleItemCount >= scrollTotalItemCount - 1) {
					onListViewDataRequested();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				scrollFirstVisibleItem = firstVisibleItem;
				scrollVisibleItemCount = visibleItemCount;
				scrollTotalItemCount = totalItemCount;
				setupResponsibleBaseAdapter(true);

				int newScrollY = estimateScrollY();
				if (isSameRow(firstVisibleItem) && isSignificantDelta(newScrollY)) {
					if (isScrollUp(newScrollY)) {
						onListViewScrollUp();
					} else {
						onListViewScrollDown();
					}
				}
			}

			private boolean isScrollUp(int newScrollY) {
				boolean scrollUp = newScrollY > mPreviousScrollY;
				mPreviousScrollY = newScrollY;
				return scrollUp;
			}

			private boolean isSignificantDelta(int newScrollY) {
				boolean isSignificantDelta = Math.abs(mLastChangeY - newScrollY) > mMinSignificantScroll;
				if (isSignificantDelta)
					mLastChangeY = newScrollY;
				return isSignificantDelta;
			}

			private boolean isSameRow(int firstVisibleItem) {
				boolean rowsChanged = firstVisibleItem == mPreviousFirstVisibleItem;
				mPreviousFirstVisibleItem = firstVisibleItem;
				return rowsChanged;
			}

			private int estimateScrollY() {
				if (listViewInstance == null || listViewInstance.getChildAt(0) == null) {
					return 0;
				}
				View topChild = listViewInstance.getChildAt(0);
				return listViewInstance.getFirstVisiblePosition() * topChild.getHeight() - topChild.getTop();
			}
		});
	}

	public ListView getListView() {
		if (listView == null) {
			listView = (ListView) findViewById(R.id.list);
			setupListViewInstance(listView);
		}
		return listView;
	}

	protected void setListView(ListView listViewInstance) {
		listView = listViewInstance;
	}

	protected void setListAdapter(BaseAdapter adapter) {
		if (listView != null) {
			listView.setAdapter(adapter);
		} else {
			Log.e(Constants.TAG, "ListView wasn't initialized !");
		}
	}

	public BaseAdapter getListAdapter() {
		BaseAdapter listAdapter = null;
		if (listView != null) {
			listAdapter = (BaseAdapter) listView.getAdapter();
		}
		return listAdapter;
	}

	protected void notifyDatasetChanged() {
		BaseAdapter listAdapter = getListAdapter();
		if (listAdapter != null) {
			listAdapter.notifyDataSetChanged();
		}
	}

	// ===================================================================================

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (useSlidingMenu() && item.getItemId() == android.R.id.home) {
			baseMenu.toggle();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	// ===================================================================================

	@Override
	public void onBackPressed() {
		Log.w(Constants.TAG, "onBackPressed, ActivityCount: " + ACTIVITY_COUNT);

		if (ACTIVITY_COUNT != 1 && useSlidingMenu() && baseMenu.isMenuShowing()) {
			baseMenu.showContent();
			return;
		} else if (ACTIVITY_COUNT == 1 && useSlidingMenu()) {
			if (!baseMenu.isMenuShowing()) {
				baseMenu.toggle();
				return;
			}
		}

		super.onBackPressed();
	}

	// ===================================================================================

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// detach tasks
		TaskManager.cancelTasks(this);
		// hide progress bar
		setProgressBarIndeterminateVisibility(false);
		// free up some memory
		ImageGetterAsync.clearDrawableCache();

		// remember activityCount (is this good idea?)
		outState.putInt(Constants.KEY_ACTIVITY_COUNT, ACTIVITY_COUNT);

		Log.w(Constants.TAG, "SAVE INSTANCE/TASK STATES");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// restore activityCount (is this good idea?)
		ACTIVITY_COUNT = savedInstanceState.getInt(Constants.KEY_ACTIVITY_COUNT);

		Log.w(Constants.TAG, "RESTORE INSTANCE/TASK STATES");
	}

	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation != currentOrientation) {
			currentOrientation = newConfig.orientation;
		}
		super.onConfigurationChanged(newConfig);
	}

	// ===================================================================================

	protected void hideUiElement(View view) {
		spawnAnimationOnElement(R.anim.fadeout, view, View.INVISIBLE);
	}

	protected void showUiElement(View view) {
		spawnAnimationOnElement(R.anim.fadein, view, View.VISIBLE);
	}

	private void spawnAnimationOnElement(int animId, final View view, final int visibilityOnDone) {
		Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), animId);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(visibilityOnDone);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		view.startAnimation(anim);
	}

	// ===================================================================================

	protected void displayReplyNotificationOnToolbar(Context context) {
		if (context == null || context.getUser() == null) {
			return;
		}

		// TODO:
	}

	protected void displayMailNotificationOnToolbar(Context context) {
		if (context == null || context.getUser() == null) {
			return;
		}

		if (toolbarMenu == null) {
			return;
		}

		final BaseActivity activity = this;

		MenuItem item = toolbarMenu.findItem(R.id.notify_mail);
		if (item == null) {
			return;
		}

		View itemView = item.getActionView();
		if (itemView == null) {
			return;
		}

		int unreadMail = context.getUser().getUnreadMail();
		if (unreadMail == 0) {
			itemView.setOnClickListener(null);
			item.setVisible(false);
		} else {
			TextView textView = itemView.findViewById(R.id.hotlist_hot);
			textView.setText(String.format("%d", unreadMail));

			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(activity, MailActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					activity.startActivity(intent);
					activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
					activity.finish();

				}
			});
			item.setVisible(true);
		}
	}

	// ===================================================================================

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

	}

	@Override
	public void onRationaleAccepted(int requestCode) {

	}

	@Override
	public void onRationaleDenied(int requestCode) {

	}
	// ===================================================================================
}
