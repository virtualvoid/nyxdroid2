package sk.virtualvoid.nyxdroid.v2;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.net.Connector;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.CommonDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.dac.UserActivityDataAccess;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import android.app.Activity;
import android.os.Bundle;
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
