package sk.virtualvoid.nyxdroid.v2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.MailNotification;
import sk.virtualvoid.nyxdroid.v2.data.Notice;
import sk.virtualvoid.nyxdroid.v2.data.NoticeType;
import sk.virtualvoid.nyxdroid.v2.data.PushNotificationResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.PushNotificationDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.PushNotificationQuery;

/**
 * @author Juraj
 */
public class GCMIntentService extends FirebaseMessagingService {
    private static final String FIREBASE_TOKEN_KEY = "FIREBASE_TOKEN";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (Connector.authorizationRequired(this)) {
            Log.w(Constants.TAG, "Authorization not complete yet !");
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mailEnabled = prefs.getBoolean("notifications_mail_enabled", true);
        boolean replyEnabled = prefs.getBoolean("notifications_reply_enabled", true);

        Intent intent = remoteMessage.toIntent();
        String type = intent.getStringExtra("type");

        if (type.equalsIgnoreCase("reply") && replyEnabled) {
            Intent broadcastIntent = new Intent(Constants.REFRESH_NOTICES_INTENT_FILTER);
            sendBroadcast(broadcastIntent);
        }

        if (type.equalsIgnoreCase("new_mail") && mailEnabled) {
            Intent broadcastIntent = new Intent(Constants.REFRESH_MAIL_INTENT_FILTER);
            broadcastIntent.putExtra(Constants.REFRESH_MAIL_COUNT, 1);
            sendBroadcast(broadcastIntent);
        }
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
