package sk.virtualvoid.nyxdroid.v2;

import org.json.JSONObject;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.query.AuthorizationQuery;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 
 * @author juraj
 * 
 */
public class LauncherActivity extends Activity implements OnClickListener {
	private static final String isRegistering = "is_registering";

	private ActionBar actionBar;
	private TextView tvResult;
	private EditText txtNick;
	private EditText txtCode;
	private Button btnGetCode;
	private Button btnFinished;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(R.style.NyxdroidTheme);
		
		if ((savedInstanceState != null && savedInstanceState.containsKey(isRegistering)) || Connector.authorizationRequired(getApplicationContext())) {
			launchAuthorization();

			if (savedInstanceState != null && (tvResult != null && txtNick != null && txtCode != null && btnGetCode != null && btnFinished != null)) {
				tvResult.setText(savedInstanceState.getString(Integer.toString(tvResult.getId())));
				txtNick.setText(savedInstanceState.getString(Integer.toString(txtNick.getId())));
				txtCode.setText(savedInstanceState.getString(Integer.toString(txtCode.getId())));
				btnGetCode.setEnabled(savedInstanceState.getBoolean(Integer.toString(btnGetCode.getId())));
				btnFinished.setEnabled(savedInstanceState.getBoolean(Integer.toString(btnFinished.getId())));
			}
		} else {
			launchDefaultActivity();
		}
	}
	
	private void launchDefaultActivity() {
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int defaultViewId = Integer.parseInt(prefs.getString("default_view", "0"));
		Bundle bundle = getIntent().getExtras();

		if(bundle != null){
			String fromFcm = bundle.getString("type");
			if(fromFcm.equalsIgnoreCase("new_mail")){
				defaultViewId = 1;
			}
			if(fromFcm.equalsIgnoreCase("reply")){
				defaultViewId = 5;
			}

		}
	
		String userName = prefs.getString(Constants.AUTH_NICK, "");
		
		Intent intent = null;

		switch (defaultViewId) {
			case 0:
				intent = new Intent(LauncherActivity.this, FeedActivity.class);
				break;
			case 1:
				intent = new Intent(LauncherActivity.this, MailActivity.class);
				break;
			case 2:
				intent = new Intent(LauncherActivity.this, BookmarksActivity.class);
				intent.putExtra(Constants.KEY_BOOKMARKS_IS_HISTORY, false);
				break;
			case 3:
				intent = new Intent(LauncherActivity.this, BookmarksActivity.class);
				intent.putExtra(Constants.KEY_BOOKMARKS_IS_HISTORY, true);
				break;
			case 4:
				intent = new Intent(LauncherActivity.this, FriendsActivity.class);
				break;
			case 5:
				intent = new Intent(LauncherActivity.this, NotificationsActivity.class);
				break;
			default:
				intent = new Intent(LauncherActivity.this, SettingsActivity.class);
				Log.e(Constants.TAG, String.format("No view defined for id: %d !", defaultViewId));
				break;
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		finish();
	}

	private void launchAuthorization() {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.authorization);
		setProgressBarIndeterminateVisibility(false);

		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(false);

		tvResult = (TextView) findViewById(R.id.authorization_activity_result);
		txtNick = (EditText) findViewById(R.id.authorization_activity_nick);
		txtCode = (EditText) findViewById(R.id.authorization_activity_code);

		btnGetCode = (Button) findViewById(R.id.authorization_activity_get_code);
		btnGetCode.setOnClickListener(this);

		btnFinished = (Button) findViewById(R.id.authorization_activity_finished);
		btnFinished.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.authorization_activity_get_code) {
			handleGetCodeButtonClick();
		}
		if (view.getId() == R.id.authorization_activity_finished) {
			handleFinishedButtonClick();
		}
	}

	private void handleGetCodeButtonClick() {
		final String authNick = txtNick.getText().toString();
		if (authNick == null || authNick.length() == 0) {
			return;
		}

		TaskWorker<AuthorizationQuery, JSONObject> worker = new TaskWorker<AuthorizationQuery, JSONObject>() {
			@Override
			public JSONObject doWork(AuthorizationQuery input) {
				Connector connector = new Connector(getContext());
				return connector.authorizationRequest(input.Nick);
			}
		};

		TaskListener<JSONObject> listener = new TaskListener<JSONObject>() {
			@Override
			public void done(JSONObject result) {
				boolean isNew = false;
				try {
					String authState = result.has("auth_state") ? result.getString("auth_state") : "";
					String authCode = result.has("auth_code") ? result.getString("auth_code") : "";
					String authToken = result.has("auth_token") ? result.getString("auth_token") : "";

					if (isNew = authState.equalsIgnoreCase("auth_new")) {
						tvResult.setText(R.string.authorization_activity_result_new);
						txtCode.setText(authCode);

						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor prefsEditor = prefs.edit();

						prefsEditor.putString(Constants.AUTH_NICK, authNick);
						prefsEditor.putString(Constants.AUTH_CODE, authCode);
						prefsEditor.putString(Constants.AUTH_TOKEN, authToken);
						prefsEditor.putBoolean(Constants.AUTH_CONFIRMED, false);
						prefsEditor.commit();
					} else if (authState.equalsIgnoreCase("auth_existing")) {
						tvResult.setText(R.string.authorization_activity_result_existing);
					} else {
						tvResult.setText(R.string.authorization_activity_result_error);
					}
				} catch (Throwable t) {
					Log.e(Constants.TAG, "authorization task listener: " + t.getMessage());
				}

				btnFinished.setEnabled(isNew);
				btnGetCode.setEnabled(!isNew);

				setProgressBarIndeterminateVisibility(false);
			}
		};

		btnGetCode.setEnabled(false);
		setProgressBarIndeterminateVisibility(true);

		Task<AuthorizationQuery, JSONObject> task = new Task<AuthorizationQuery, JSONObject>(LauncherActivity.this, worker, listener);
		TaskManager.startTask(task, new AuthorizationQuery(authNick));
	}

	private void handleFinishedButtonClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.warning);
		builder.setMessage(R.string.are_you_sure_authorization_is_complete);
		
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean(Constants.AUTH_CONFIRMED, true);
				prefsEditor.commit();

				launchDefaultActivity();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (tvResult == null || txtNick == null || txtCode == null || btnGetCode == null || btnFinished == null) {
			return;
		}

		outState.putBoolean(isRegistering, true);
		outState.putString(Integer.toString(tvResult.getId()), tvResult.getText().toString());
		outState.putString(Integer.toString(txtNick.getId()), txtNick.getText().toString());
		outState.putString(Integer.toString(txtCode.getId()), txtCode.getText().toString());
		outState.putBoolean(Integer.toString(btnGetCode.getId()), btnGetCode.isEnabled());
		outState.putBoolean(Integer.toString(btnFinished.getId()), btnFinished.isEnabled());
	}
}
