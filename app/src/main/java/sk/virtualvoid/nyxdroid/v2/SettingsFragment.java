package sk.virtualvoid.nyxdroid.v2;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.google.firebase.messaging.FirebaseMessaging;

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

        SwitchPreference notificationsEnabled = findPreference("notifications_enabled");
        if (notificationsEnabled != null) {
            notificationsEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final boolean enabled = (Boolean) newValue;
                    if (enabled) {
                        GCMIntentServiceHelper.enableNotifications(getContext(), FirebaseMessaging.getInstance());
                    } else {
                        GCMIntentServiceHelper.disableNotifications(getContext(), FirebaseMessaging.getInstance());
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
}
