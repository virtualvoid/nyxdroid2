package sk.virtualvoid.nyxdroid.v2;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import sk.virtualvoid.nyxdroid.library.Constants;

public class GCMIntentServiceHelper {
    public static void enableNotifications(final Context context, FirebaseMessaging messaging) {
        Task<String> tokenTask = messaging.getToken();
        tokenTask.addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(Constants.TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                String token = task.getResult();
                GCMIntentService.firePushNotificationRegister(context, token, false);
            }
        });
    }

    public static void disableNotifications(final Context context, FirebaseMessaging messaging) {
        Task<Void> deleteTokenTask = messaging.deleteToken();

        deleteTokenTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                GCMIntentService.firePushNotificationUnregister(context);
            }
        });
    }
}
