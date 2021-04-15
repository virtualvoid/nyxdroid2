package sk.virtualvoid.nyxdroid.v2;

import java.io.File;
import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.CommonDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.dac.UserActivityDataAccess;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gcm.GCMRegistrar;

/**
 * 
 * @author Juraj
 * 
 */
public class SettingsActivity extends BaseActivity {
	private static final String SETTINGS_FRAGMENT_TAG = "nd2_settingsf";

	public SettingsFragment settingsFragment;
	
	@Override
	protected int getContentViewId() {
		return R.layout.settings;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		settingsFragment = 	(SettingsFragment) fm.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
		if (settingsFragment == null) {
			settingsFragment = new SettingsFragment();
		}
		
		if (!settingsFragment.isAdded()) {
			ft.add(R.id.settings_content, new SettingsFragment());
		}

		ft.show(settingsFragment);
		
		ft.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.send_application_log:
				return sendApplicationLog();
			case R.id.clear_credentials:
				return clearCredentials();
			case R.id.clear_drawable_cache:
				return clearDrawableCache();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId) {
		/* Not needed here */
		return false;
	}

	private boolean sendApplicationLog() {
		try {
			PackageManager packageManager = getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "juraj.suchan@gmail.com" });
			intent.putExtra(Intent.EXTRA_SUBJECT, String.format("nyxdroid %d log", packageInfo.versionCode));

			StringBuilder sb = new StringBuilder();
			sb.append(getString(R.string.im_sending_you_application_log));
			sb.append(System.getProperty("line.separator"));
			sb.append(String.format("Device: %s, Model: %s, Manufacturer: %s, OS:%s / %s", android.os.Build.DEVICE, android.os.Build.MODEL, android.os.Build.MANUFACTURER, System.getProperty("os.name"), System.getProperty("os.version")));

			intent.putExtra(Intent.EXTRA_TEXT, sb.toString());

			File file = new File(NyxdroidApplication.logPath);
			if (!file.exists()) {
				Toast.makeText(this, R.string.application_log_wasnt_created_yet, Toast.LENGTH_LONG).show();
			} else {
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

				startActivity(Intent.createChooser(intent, getString(R.string.send_application_log_title)));
			}
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.you_dont_have_email_client_installed, Toast.LENGTH_LONG).show();
		} catch (NameNotFoundException e) {
			Log.e(Constants.TAG, "sendApplicationLog error: " + e.getMessage());
		}
		return true;
	}

	private boolean clearCredentials() {
		Task<ITaskQuery, NullResponse> task = UserActivityDataAccess.clearCredentials(SettingsActivity.this, new ClearCredentialsTaskListener());
		TaskManager.startTask(task, ITaskQuery.empty);
		return true;
	}

	private boolean clearDrawableCache() {
		Task<ITaskQuery, NullResponse> task = CommonDataAccess.clearDrawableCache(SettingsActivity.this, new ClearDrawableCacheTaskListener());
		TaskManager.startTask(task, ITaskQuery.empty);
		return true;
	}
	
	/**
	 * 
	 * @author Juraj
	 *
	 */
	private class ClearCredentialsTaskListener extends TaskListener<NullResponse> {
		@Override
		public void done(NullResponse output) {
			Activity context = (Activity) getContext();

			GCMRegistrar.setRegisteredOnServer(context, false);

			Connector.authorizationRemove(context);

			context.finish();
		}
	}
	
	private class ClearDrawableCacheTaskListener extends TaskListener<NullResponse> {
		@Override
		public void done(NullResponse output) {
			Toast.makeText(getContext(), R.string.done, Toast.LENGTH_SHORT).show();
		}
	}

}
