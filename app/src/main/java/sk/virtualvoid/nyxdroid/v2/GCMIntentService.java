package sk.virtualvoid.nyxdroid.v2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.PushNotificationResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.PushNotificationDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.PushNotificationQuery;

/**
 * 
 * @author Juraj
 * 
 */
public class GCMIntentService extends FirebaseMessagingService  {
	private static final String FIREBASE_TOKEN_KEY = "FIREBASE_TOKEN";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		if (Connector.authorizationRequired(this)) {
			Log.w(Constants.TAG, "Authorization not complete yet !");
			return;
		}

		// TODO:
	}

	@Override
	public void onNewToken(final String token) {
		super.onNewToken(token);

		firePushNotificationRegister(GCMIntentService.this, token, true);
	}

	public static void firePushNotificationRegister(final Context context, final String token, final boolean overwrite) {
		Task<PushNotificationQuery, PushNotificationResponse> task = PushNotificationDataAccess.register(
				context,
				new TaskListener<PushNotificationResponse>() {
					@Override
					public void done(PushNotificationResponse response) {
						rememberPushNotificationToken(context, token, overwrite);
					}
				}
		);

		TaskManager.startTask(task, new PushNotificationQuery(token));
	}

	public static void rememberPushNotificationToken(Context context, String token, boolean overwrite) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String oldToken = prefs.getString(FIREBASE_TOKEN_KEY, null);
		if (oldToken != null && !overwrite) {
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(FIREBASE_TOKEN_KEY, token);
		editor.commit();
	}
}
