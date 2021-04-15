package sk.virtualvoid.nyxdroid.v2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import sk.virtualvoid.nyxdroid.v2.internal.PushNotificationRegistrar;

public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {
        super();
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        addPreferencesFromResource(R.xml.settings);
//        Preference button = findPreference("notifications_clear_token");
//        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                FirebaseMessaging.getInstance().getToken()
//                        .addOnCompleteListener(new OnCompleteListener<String>() {
//                            @Override
//                            public void onComplete(@NonNull Task<String> task) {
//                                if (!task.isSuccessful()) {
//                                    Log.w("TAGTAG", "Fetching FCM registration token failed", task.getException());
//                                    return;
//                                }
//                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                                String token = prefs.getString("FIREBASE_TOKEN", null);
//                                prefs.edit().putString("FIREBASE_TOKEN", task.getResult()).apply();
//                                Log.i("TAGTAG", task.getResult());
//                                PushNotificationRegistrar.register(getActivity(), task.getResult());
//                            }});
//                return true;
//            }
//        });
//    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

    }
}
