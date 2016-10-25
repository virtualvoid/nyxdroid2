package sk.virtualvoid.nyxdroid.v2.internal;

import org.apache.log4j.Logger;

import sk.virtualvoid.nyxdroid.v2.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

/**
 * 
 * @author Juraj
 * 
 */
public class GooglePlayRating implements DialogInterface.OnClickListener {
	private final static Logger log = Logger.getLogger(GooglePlayRating.class);
	private Context context;

	private final int minLaunchesUntilPrompt = 100;
	private final int minLaunchesUntilNextPrompt = 50;
	private final int minDaysUntilPrompt = 7;
	private final int minDaysUntilNextPrompt = 5;

	private SharedPreferences prefs;

	private GooglePlayRating(Context context) {
		this.context = context;
	}

	public void execute() {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (prefs.getBoolean(DONT_ASK_AGAIN, false)) {
			return;
		}

		long now = System.currentTimeMillis();

		SharedPreferences.Editor editor = prefs.edit();

		int totalLaunchCount = prefs.getInt(TOTAL_LAUNCH_COUNT, 0) + 1;
		editor.putInt(TOTAL_LAUNCH_COUNT, totalLaunchCount);

		long firstLaunchTime = prefs.getLong(FIRST_LAUNCH_TIME, 0);
		if (firstLaunchTime == 0) {
			firstLaunchTime = now;

			editor.putLong(FIRST_LAUNCH_TIME, firstLaunchTime);
		}

		long lastPromptTime = prefs.getLong(LAST_PROMPT_TIME, 0);

		int launchesLastPrompt = prefs.getInt(LAUNCHES_LAST_PROMPT, 0) + 1;
		editor.putInt(LAUNCHES_LAST_PROMPT, launchesLastPrompt);

		boolean result = false;

		if (totalLaunchCount >= minLaunchesUntilPrompt && (now - firstLaunchTime >= minDaysUntilPrompt * DateUtils.DAY_IN_MILLIS)) {
			if (lastPromptTime == 0 || (launchesLastPrompt >= minLaunchesUntilNextPrompt && (now - lastPromptTime >= minDaysUntilNextPrompt * DateUtils.DAY_IN_MILLIS))) {
				editor.putLong(LAST_PROMPT_TIME, now);
				editor.putInt(LAUNCHES_LAST_PROMPT, 0);
				result = true;
			}
		}

		editor.commit();

		if (result) {
			show(prefs);
		}
	}

	private void show(SharedPreferences prefs) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(R.string.hi_there);
		dialogBuilder.setMessage(R.string.gp_rating_message);

		dialogBuilder.setPositiveButton(R.string.rate_it, this);
		dialogBuilder.setNeutralButton(R.string.not_now, this);
		dialogBuilder.setNegativeButton(R.string.never, this);

		AlertDialog dialog = dialogBuilder.create();
		dialog.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		SharedPreferences.Editor editor = prefs.edit();

		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				try {
					context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%s", context.getPackageName()))));

					editor.putBoolean(DONT_ASK_AGAIN, true);
					editor.commit();
				} catch (Throwable e) {
					log.error(e);
				}
				return;

			case DialogInterface.BUTTON_NEUTRAL:
				return;

			case DialogInterface.BUTTON_NEGATIVE:
				log.debug("user decided not to rate application. ever. faggot!");

				editor.putBoolean(DONT_ASK_AGAIN, true);
				editor.commit();
				return;
		}
	}

	public static GooglePlayRating getGooglePlayRating(Context context) {
		GooglePlayRating googlePlayRating = new GooglePlayRating(context);
		return googlePlayRating;
	}

	private static final String DONT_ASK_AGAIN = "gpr_dont_ask_again";
	private static final String TOTAL_LAUNCH_COUNT = "gpr_total_launch_count";
	private static final String FIRST_LAUNCH_TIME = "gpr_first_launch_time";
	private static final String LAST_PROMPT_TIME = "gpr_last_prompt_time";
	private static final String LAUNCHES_LAST_PROMPT = "gpr_launches_last_prompt";
}
