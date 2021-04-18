package sk.virtualvoid.nyxdroid.v2;

import android.content.Context;
import android.os.Environment;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.library.Constants;

/**
 * 
 * @author Juraj
 * 
 */
public class NyxdroidApplication extends MultiDexApplication {
	public static final String logPath = Environment.getExternalStorageDirectory() + File.separator + "nyxdroid.log";

	private static final Logger log = Logger.getLogger(NyxdroidApplication.class);
	private static final UncaughtExceptionHandler defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
	private static Context context;

	public NyxdroidApplication() {
	}

	public static Context getAppContext() {
		return NyxdroidApplication.context;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		NyxdroidApplication.context = getApplicationContext();
		try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
			Log.e(Constants.TAG, "Unable to initialize android.os.AsyncTask !");
		}

		try {
			ProviderInstaller.installIfNeeded(this);
		} catch (GooglePlayServicesRepairableException e) {
			GoogleApiAvailability.getInstance()
					.showErrorNotification(this, e.getConnectionStatusCode());
		} catch (GooglePlayServicesNotAvailableException e) {
			Log.e(Constants.TAG, "Unable to initialize TLS 1.2!");
		}

		FirebaseAnalytics.getInstance(getApplicationContext())
				.setAnalyticsCollectionEnabled(true);

		FirebaseMessaging.getInstance()
				.getToken()
				.addOnCompleteListener(new OnCompleteListener<String>() {
					@Override
					public void onComplete(@NonNull Task<String> task) {
						if (!task.isSuccessful()) {
							Log.w(Constants.TAG, "Fetching FCM registration token failed", task.getException());
							return;
						}

						String token = task.getResult();
						GCMIntentService.firePushNotificationRegister(getApplicationContext(), token, false);
					}
				});

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

		try {
			final LogConfigurator logConfigurator = new LogConfigurator();
			logConfigurator.setFileName(logPath);
			logConfigurator.setRootLevel(Level.ALL);
			logConfigurator.setUseLogCatAppender(true);
			logConfigurator.configure();
		} catch (Throwable e/* gotta catch'em all */) {
			Log.e(Constants.TAG, String.format("UNABLE TO CONFIGURE LOG4J: %s", e.getMessage()));
		}
		
		Thread.setDefaultUncaughtExceptionHandler(new CustomUncaughtExceptionHandler());
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
		log.warn("onLowMemory!");
		ImageGetterAsync.clearDrawableCache();
		super.onLowMemory();
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	public static class CustomUncaughtExceptionHandler implements UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			log.fatal("Uncaught exception !", ex);

			if (defaultExceptionHandler != null) {
				defaultExceptionHandler.uncaughtException(thread, ex);
			}
		}
	}
}
