package sk.virtualvoid.nyxdroid.v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import de.mindpipe.android.logging.log4j.LogConfigurator;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.library.Constants;
import android.app.Application;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

/**
 * 
 * @author Juraj
 * 
 */
public class NyxdroidApplication extends MultiDexApplication {
	public static final String logPath = Environment.getExternalStorageDirectory() + File.separator + "nyxdroid.log";

	private static final Logger log = Logger.getLogger(NyxdroidApplication.class);
	private static final UncaughtExceptionHandler defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

	public NyxdroidApplication() {
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

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
