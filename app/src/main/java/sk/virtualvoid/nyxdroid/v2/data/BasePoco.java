package sk.virtualvoid.nyxdroid.v2.data;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class BasePoco {
	public Long Id;
	public String Nick;
	public boolean IsMine;

	public static Date timeToLocal(long time) {
		Date dt = new Date();
		dt.setTime((time * 1000));
		return dt;
	}

	public static long timeNow() {
		Date dt = new Date();
		return dt.getTime() / 1000;
	}

	public static String timeToString(Context context, long timestamp) {
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTime(timeToLocal(timestamp));

		String result = "";

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
	
		result = String.format(context.getString(R.string.timeformat_normal), day, month, year, hour, minute, second);

		return result;
	}

	public static String nickToUrl(String nick, Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Boolean overrideSsl = prefs.getBoolean(Constants.SETTINGS_SSL_OVERRIDE, false);
		String https = "https";
		if(overrideSsl){
			https = "http";
		}
		String nickup = nick.toUpperCase(), result = https+ "://i.nyx.cz/" + nickup.charAt(0) + "/" + nickup + ".gif";
		return result;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
