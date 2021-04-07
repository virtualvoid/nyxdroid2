package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.widgets.ISecondBaseMenu;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.MailNotification;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.MailDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.dac.UserActivityDataAccess;
import sk.virtualvoid.nyxdroid.v2.internal.Appearance;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

/**
 * 
 * @author Juraj
 * 
 */
public class BaseMenu {
	private BaseActivity activity;
	private SlidingMenu slidingMenu;
	private Appearance appearance;

	private BaseMenuItemAdapter adapter;

	private GetMailNotificationTaskListener mailNotificationTaskListener = new GetMailNotificationTaskListener();
	private Task<ITaskQuery, MailNotification> mailNotificationTask = null;

	public BaseMenu(BaseActivity activity) {
		this.activity = activity;
	}

	public void initialize() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		final String nick = prefs.getString(Constants.AUTH_NICK, "");

		appearance = Appearance.getAppearance(prefs);

		slidingMenu = new SlidingMenu(activity);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		// slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.setBehindWidthRes(R.dimen.slidingmenu_width);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.attachToActivity(activity, SlidingMenu.SLIDING_WINDOW);

		LayoutInflater inflater = activity.getLayoutInflater();
		View view = inflater.inflate(R.layout.main_menu, null);

		final ImageView ivIcon = (ImageView) view.findViewById(R.id.menu_frame_icon);

		final ImageDownloader imageDownloader = new ImageDownloader(activity, activity.getResources().getDrawable(R.drawable.empty_avatar));
		// imageDownloader.setMode(Mode.CORRECT);

		slidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
			@Override
			public void onOpened() {
				// start mail checking only if not in mail
				if (!(activity instanceof MailActivity)) {
					TaskManager.killIfNeeded(mailNotificationTask);

					// TODO: maily
					//mailNotificationTask = MailDataAccess.getNotifications(activity, mailNotificationTaskListener);

					TaskManager.startTask(mailNotificationTask, ITaskQuery.empty);
				}
				imageDownloader.download(BasePoco.nickToUrl(nick, activity), ivIcon);
			}
		});

		TextView tvNick = (TextView) view.findViewById(R.id.menu_frame_nick);
		tvNick.setText(nick.toUpperCase());

		ListView listView = (ListView) view.findViewById(R.id.menu_frame_list);
		listView.setAdapter(adapter = new BaseMenuItemAdapter());
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				handleMenu(id);
			}
		});

		slidingMenu.setMenu(view);
	}

	public void initializeSecondMenu() {
		if (activity instanceof ISecondBaseMenu) {
			slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
			slidingMenu.setSecondaryMenu(((ISecondBaseMenu) activity).getSecondBaseMenu());
			slidingMenu.setSecondaryShadowDrawable(R.drawable.shadowright);

			slidingMenu.setSecondaryOnOpenListner(new SlidingMenu.OnOpenListener() {
				@Override
				public void onOpen() {
					((ISecondBaseMenu) activity).onSecondBaseMenuOpened();
				}
			});
		}
	}

	public void toggle() {
		if (slidingMenu == null) {
			return;
		}
		slidingMenu.toggle();
	}

	public boolean isMenuShowing() {
		return slidingMenu != null && (slidingMenu.isMenuShowing() || slidingMenu.isSecondaryMenuShowing());
	}

	public void showContent() {
		if (slidingMenu == null) {
			return;
		}

		slidingMenu.showContent();
	}

	public void setBackgroundColor(String colorCode) {
		if (slidingMenu != null) {
			slidingMenu.setBackgroundColor(Color.parseColor(colorCode));
		}
	}

	private void handleMenu(long itemId) {
		Intent intent = null;

		if (itemId == BaseMenuItem.ID_FEED && !(activity instanceof FeedActivity)) {
			intent = new Intent(activity, FeedActivity.class);
		}

		if (itemId == BaseMenuItem.ID_MAIL && !(activity instanceof MailActivity)) {
			intent = new Intent(activity, MailActivity.class);
			// clear when read
			mailNotificationTaskListener.clear();
		}

		if (itemId == BaseMenuItem.ID_BOOKMARKS) {
			intent = new Intent(activity, BookmarksActivity.class);
			intent.putExtra(Constants.KEY_BOOKMARKS_IS_HISTORY, false);
		}

		if (itemId == BaseMenuItem.ID_HISTORY) {
			intent = new Intent(activity, BookmarksActivity.class);
			intent.putExtra(Constants.KEY_BOOKMARKS_IS_HISTORY, true);
		}

		if (itemId == BaseMenuItem.ID_FRIENDS && !(activity instanceof FriendsActivity)) {
			intent = new Intent(activity, FriendsActivity.class);
		}

		if (itemId == BaseMenuItem.ID_NOTIFICATIONS && !(activity instanceof NotificationsActivity)) {
			intent = new Intent(activity, NotificationsActivity.class);
		}

		if (itemId == BaseMenuItem.ID_SEARCH && !(activity instanceof SearchActivity)) {
			intent = new Intent(activity, SearchActivity.class);
		}

		if (itemId == BaseMenuItem.ID_EVENTS && !(activity instanceof EventsActivity)) {
			intent = new Intent(activity, EventsActivity.class);
		}

		if (itemId == BaseMenuItem.ID_SETTINGS && !(activity instanceof SettingsActivity)) {
			intent = new Intent(activity, SettingsActivity.class);
		}

		if (itemId == BaseMenuItem.ID_ABOUT && !(activity instanceof InformationActivity)) {
			intent = new Intent(activity, InformationActivity.class);
		}

		if (itemId == BaseMenuItem.ID_EXIT) {
			Task<ITaskQuery, NullResponse> logoutTask = UserActivityDataAccess.inactivate(activity, new TaskListener<NullResponse>() {
				@Override
				public void done(NullResponse output) {
					ImageGetterAsync.clearDrawableCache();
					activity.finish();
				}
			});
			TaskManager.startTask(logoutTask, ITaskQuery.empty);
			return;
		}

		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(intent);
			activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			activity.finish();
		}
	}

	private class BaseMenuItem {
		public long Id;

		public int IconResLight;
		public int IconResDark;

		public int TitleRes;
		public String TitleAdditional;

		public BaseMenuItem(long id, int iconResLight, int iconResDark, int titleRes) {
			this.Id = id;
			this.IconResLight = iconResLight;
			this.IconResDark = iconResDark;
			this.TitleRes = titleRes;
		}

		public static final long ID_FEED = 0;
		public static final long ID_MAIL = 1;
		public static final long ID_BOOKMARKS = 2;
		public static final long ID_HISTORY = 3;
		public static final long ID_FRIENDS = 4;
		public static final long ID_NOTIFICATIONS = 5;
		public static final long ID_SEARCH = 6;
		public static final long ID_SETTINGS = 7;
		public static final long ID_ABOUT = 8;
		public static final long ID_EXIT = 9;
		public static final long ID_EVENTS = 10;
	}

	private class BaseMenuItemAdapter extends BaseAdapter {
		private ArrayList<BaseMenuItem> model;

		public BaseMenuItemAdapter() {
			model = new ArrayList<BaseMenu.BaseMenuItem>();
			//model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_FEED, R.drawable.light_action_feed, R.drawable.dark_action_feed, R.string.app_name_feed));
			//model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_MAIL, R.drawable.light_action_mail, R.drawable.dark_action_mail, R.string.app_name_mail));
			model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_BOOKMARKS, R.drawable.light_action_bookmark, R.drawable.dark_action_bookmark, R.string.app_name_bookmarks));
			model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_HISTORY, R.drawable.light_action_clock, R.drawable.dark_action_clock, R.string.app_name_bookmarks_history));
			//model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_FRIENDS, R.drawable.light_action_users, R.drawable.dark_action_users, R.string.app_name_friends));
			//model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_NOTIFICATIONS, R.drawable.light_action_bulb, R.drawable.dark_action_bulb, R.string.app_name_notifications));
			//model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_SEARCH, R.drawable.light_action_search, R.drawable.dark_action_search, R.string.app_name_search));
			//model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_EVENTS, R.drawable.light_action_calendar_day, R.drawable.dark_action_calendar_day, R.string.app_name_events));
			model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_SETTINGS, R.drawable.light_action_gear, R.drawable.dark_action_gear, R.string.app_name_settings));
			model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_ABOUT, R.drawable.light_action_info, R.drawable.dark_action_info, R.string.app_name_about));
			//model.add(new BaseMenuItem(BaseMenu.BaseMenuItem.ID_EXIT, R.drawable.light_action_exit, R.drawable.dark_action_exit, R.string.app_name_exit));
		}

		@Override
		public int getCount() {
			return model.size();
		}

		@Override
		public Object getItem(int position) {
			return model.get(position);
		}

		public BaseMenuItem getItemById(long id) {
			for (BaseMenuItem item : model) {
				if (item.Id == id) {
					return item;
				}
			}

			return null;
		}

		@Override
		public long getItemId(int position) {
			return model.get(position).Id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewHolder holder = null;

			if (row == null) {
				holder = new ViewHolder();

				row = activity.getLayoutInflater().inflate(R.layout.main_menu_row, parent, false);
				holder.Icon = (ImageView) row.findViewById(R.id.menu_frame_row_icon);
				holder.Title = (TextView) row.findViewById(R.id.menu_frame_row_title);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			BaseMenuItem item = (BaseMenuItem) getItem(position);

			String titleRes = activity.getResources().getString(item.TitleRes);
			String titleAdd = item.TitleAdditional;
			String title = titleAdd == null ? titleRes : String.format("%s %s", titleRes, titleAdd);

			holder.Icon.setImageResource(appearance.getUseDarkTheme() ? item.IconResDark : item.IconResLight);
			holder.Title.setText(Html.fromHtml(title));

			return row;
		}
	}

	private class ViewHolder {
		public ImageView Icon;
		public TextView Title;
	}

	private class GetMailNotificationTaskListener extends TaskListener<MailNotification> {
		@Override
		public void done(MailNotification output) {
			if (!output.valid()) {
				return;
			}

			BaseMenuItem item = adapter.getItemById(BaseMenu.BaseMenuItem.ID_MAIL);
			item.TitleAdditional = String.format("(<font color='red'>%d</font>)", output.Count);

			adapter.notifyDataSetChanged();
		}

		public void clear() {
			BaseMenuItem item = adapter.getItemById(BaseMenu.BaseMenuItem.ID_MAIL);
			item.TitleAdditional = null;
		}
	}
}
