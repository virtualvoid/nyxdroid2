package sk.virtualvoid.nyxdroid.v2;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.lang.Thread.UncaughtExceptionHandler;

import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.library.Constants;

/**
 * @author Juraj
 */
public class NyxdroidApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String authNick = prefs.getString(Constants.AUTH_NICK, null);
        final boolean notificationsEnabled = prefs.getBoolean(Constants.NOTIFICATIONS_ENABLED, true);

        asyncTaskFix();
        providerInstallerFix();

        initializeCrashlytics(authNick);
        initializeAnalytics(authNick);
        initializeMessaging(notificationsEnabled);
        initializeImageLoader();
    }

    @Override
    public void onTrimMemory(int level) {
        if (level >= Constants.MEMORY_TRIM_LEVEL) {
            ImageGetterAsync.clearDrawableCache();
        }
        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        ImageGetterAsync.clearDrawableCache();
        super.onLowMemory();
    }

    private void asyncTaskFix() {
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            Log.e(Constants.TAG, "Unable to initialize android.os.AsyncTask !");
        }
    }

    private void providerInstallerFix() {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance()
                    .showErrorNotification(this, e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(Constants.TAG, "Unable to initialize TLS 1.2!");
        } catch (Throwable e) {
            Log.e(Constants.TAG, String.format("providerInstallerFix: %s", e.getMessage()));
        }
    }

    private void initializeAnalytics(String authNick) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        analytics.setAnalyticsCollectionEnabled(true);
        analytics.setUserId(authNick);
    }

    private void initializeCrashlytics(String authNick) {
        final FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(true);
        crashlytics.setUserId(authNick);
        Task<Boolean> checkTask = crashlytics.checkForUnsentReports();
        checkTask.addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                boolean unsentReports = task.getResult();
                if (unsentReports) {
                    crashlytics.sendUnsentReports();
                    crashlytics.deleteUnsentReports();
                }
            }
        });

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                crashlytics.log(String.format("ERROR: %s, STACKTRACE: %s", e.getMessage(), e.toString()));
            }
        });
    }

    private void initializeMessaging(final boolean notificationsEnabled) {
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();

        if (notificationsEnabled) {
            GCMIntentServiceHelper.enableNotifications(getApplicationContext(), messaging);
        } else {
            GCMIntentServiceHelper.disableNotifications(getApplicationContext(), messaging);
        }
    }

    private void initializeImageLoader() {
        DisplayImageOptions ilOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true)
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration ilConfig = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(ilOptions)
                .discCacheFileCount(Constants.ImageLoader.DiscCacheFileCount)
                .memoryCacheExtraOptions(Constants.ImageLoader.MemoryCacheMaxWidth, Constants.ImageLoader.MemoryCacheMaxHeight)
                .denyCacheImageMultipleSizesInMemory()
                .build();

        ImageLoader.getInstance().init(ilConfig);
    }
}
