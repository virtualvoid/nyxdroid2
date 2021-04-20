package sk.virtualvoid.nyxdroid.v2;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import sk.virtualvoid.nyxdroid.library.Constants;

public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getActivity();

        Preference button = findPreference("notifications_clear_token");
        if (button == null) {
            Log.e(Constants.TAG, "Ehm, clear token button not found.");
            return;
        }

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FirebaseMessaging
                        .getInstance()
                        .getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(Constants.TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                String token = task.getResult();
                                GCMIntentService.firePushNotificationRegister(context, token, true);
                            }
                        });

                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }
}
