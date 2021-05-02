package sk.virtualvoid.nyxdroid.v2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import sk.virtualvoid.nyxdroid.library.Constants;

public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {
        super();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SwitchPreferenceCompat notificationsEnabled = findPreference("notifications_enabled");
        if (notificationsEnabled != null) {
            notificationsEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final boolean enabled = (Boolean) newValue;
                    if (enabled) {
                        registerForNotifications();
                    } else {
                        unregisterFromNotifications();
                    }
                    return true;
                }
            });
        }

        SeekBarPreference fontSizeSeekbar = findPreference("font_size_seekbar");
        if (fontSizeSeekbar != null) {
            fontSizeSeekbar.setMin(12);
            fontSizeSeekbar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final String progress = String.valueOf(newValue);
                    SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
                    prefs.edit().putString("font_size", progress).apply();
                    return true;
                }
            });
        }
    }

    private void unregisterFromNotifications() {
        GCMIntentService.firePushNotificationUnregister(getContext());
    }

    private void registerForNotifications() {
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();

        Task<String> tokenTask = messaging.getToken();
        tokenTask.addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(Constants.TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                String token = task.getResult();
                GCMIntentService.firePushNotificationRegister(getContext(), token, true);
            }
        });
    }
}
