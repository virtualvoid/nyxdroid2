package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.MailNotification;
import sk.virtualvoid.nyxdroid.v2.data.Notice;
import sk.virtualvoid.nyxdroid.v2.data.NoticeType;
import sk.virtualvoid.nyxdroid.v2.data.dac.MailDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.dac.NoticeDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.NoticeQuery;
import sk.virtualvoid.nyxdroid.v2.internal.PushNotificationRegistrar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

/**
 * 
 * @author Juraj
 * 
 */
public class GCMIntentService extends GCMBaseIntentService {
	private static final GetMailNotificationsListener getMailNotificationsListener = new GetMailNotificationsListener();
	private static final GetReplyNotificationsListener getReplyNotificationsListener = new GetReplyNotificationsListener();

	public GCMIntentService() {
		super(Constants.GCM_SENDER_ID);
	}

	private static void generateNotification(Context context, int id, String title, String text, PendingIntent pendingIntent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String ringtoneUrl = prefs.getString("notification_ringtone", "");

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(title);
		builder.setContentText(text);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.ic_stat_notification);
		builder.setContentIntent(pendingIntent);
		builder.setWhen(System.currentTimeMillis());

		if (ringtoneUrl.length() == 0) {
			builder.setDefaults(Notification.DEFAULT_ALL);
		} else {
			builder.setSound(Uri.parse(ringtoneUrl));
			builder.setVibrate(new long[] { 100, 200, 300, 400, 500 });
		}

		Notification notification = builder.getNotification();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notification);
	}

	@Override
	protected void onError(Context context, String message) {
		Log.e(Constants.TAG, "GCM ERROR: " + message);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		if (Connector.authorizationRequired(context)) {
			Log.w(Constants.TAG, "Authorization not complete yet !");
			return;
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean mailEnabled = prefs.getBoolean("notifications_mail_enabled", true);
		boolean replyEnabled = prefs.getBoolean("notifications_reply_enabled", true);

		if (intent.hasExtra("type")) {
			String type = intent.getStringExtra("type");

			if (type.equalsIgnoreCase("new_mail") && mailEnabled) {
				Task<ITaskQuery, MailNotification> task = MailDataAccess.getNotifications(context, getMailNotificationsListener);
				TaskManager.startTask(task, ITaskQuery.empty);
			}

			if (type.equalsIgnoreCase("reply") && replyEnabled) {
				NoticeQuery query = new NoticeQuery();
				query.KeepNew = true;

				Task<NoticeQuery, ArrayList<Notice>> task = NoticeDataAccess.getNotifications(context, getReplyNotificationsListener);
				TaskManager.startTask(task, query);
			}

			Log.d(Constants.TAG, String.format("GCM/NYX push message: %s", type));
		} else {
			Log.d(Constants.TAG, "Ignoring GCM/NYX push message");
		}
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.d(Constants.TAG, "GCM Registered: " + registrationId);
		PushNotificationRegistrar.register(context, registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.d(Constants.TAG, "GCM Unregistered: " + registrationId);
		PushNotificationRegistrar.unregister(context);
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	private static class GetMailNotificationsListener extends TaskListener<MailNotification> {
		private final static Logger log = Logger.getLogger(GetMailNotificationsListener.class);

		@Override
		public void handleError(Throwable t) {
			log.error("Mail notification handle error", t);
		}

		@Override
		public void done(MailNotification data) {
			if (!data.valid()) {
				return;
			}

			Context context = getContext();

			Intent launchIntent = new Intent(context, MailActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);

			String title = "New mail !";
			String text = String.format("You have %d new mail(s), last from %s.", data.Count, data.LastFrom);

			Intent intent = new Intent(Constants.REFRESH_MAIL_INTENT_FILTER);
			intent.putExtra(Constants.REFRESH_MAIL_COUNT, data.Count);
			context.sendBroadcast(intent);

			GCMIntentService.generateNotification(context, 0, title, text, pendingIntent);
		}
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	private static class GetReplyNotificationsListener extends TaskListener<ArrayList<Notice>> {
		private final static Logger log = Logger.getLogger(GetReplyNotificationsListener.class);

		@Override
		public void handleError(Throwable t) {
			log.error("Reply notifications handle error", t);
		}

		@Override
		public void done(ArrayList<Notice> output) {
			if (output.size() == 0) {
				return;
			}

			Context context = getContext();

			Intent launchIntent = new Intent(context, NotificationsActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);

			ArrayList<Notice> replies = new ArrayList<Notice>();
			for (Notice notice : output) {
				if (notice.Type != NoticeType.REPLY || (notice.Type == NoticeType.REPLY && !notice.IsNew)) {
					continue;
				}

				replies.add(notice);
			}

			int id = 0;
			if (replies.size() > 3) {
				String title = "New replies !";
				String text = String.format("You have %d new replies...", replies.size());

				GCMIntentService.generateNotification(context, ++id, title, text, pendingIntent);
			} else {
				for (Notice reply : replies) {
					String title = reply.Nick;
					String text = reply.Content;

					GCMIntentService.generateNotification(context, ++id, title, text, pendingIntent);
				}
			}
		}
	}
}
