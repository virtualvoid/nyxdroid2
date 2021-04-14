package sk.virtualvoid.nyxdroid.v2.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class BasePoco {
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public Long Id;
	public String Nick;
	public boolean IsMine;

	public static long timeFromString(String input) {
		try {
			return dateFormatter.parse(input).getTime();
		} catch (Throwable t) {
			return -1; // java, meh
		}
	}

	public static Date timeToLocal(long time) {
		Date dt = new Date();
		dt.setTime(time);
		return dt;
	}

	public static long timeNow() {
		Date dt = new Date();
		return dt.getTime();
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
		String nickUp = nick.toUpperCase(), result = "https://nyx.cz/" + nickUp.charAt(0) + "/" + nickUp + ".gif";
		return result;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
