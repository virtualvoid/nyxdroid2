package sk.virtualvoid.nyxdroid.v2.data;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

/**
 * 
 * @author Juraj
 * 
 */
public class UserActivity implements Parcelable {
	public static final long InvalidTime = -1;

	public long Time;
	public String Location;
	public String LocationUrl;

	private UserActivity() {
		Time = InvalidTime;
		Location = null;
		LocationUrl = null;
	}

	public UserActivity(Parcel source) {
		this.Time = source.readLong();
		this.Location = source.readString();
		this.LocationUrl = source.readString();
	}

	public boolean valid() {
		return Time != InvalidTime;
	}

	public Long discussion() {
		if (LocationUrl == null || !LocationUrl.contains("topic")) {
			return null;
		}

		final String locId = ";id=";

		int index = LocationUrl.lastIndexOf(locId);
		if (index == -1) {
			return null;
		}

		try {
			String strId = LocationUrl.substring(index + locId.length()).trim();
			return Long.parseLong(strId);
		} catch (Throwable e) {
			// Noop.
		}

		return null;
	}

	public String toRelativeTimeSpanString(Date now) {
		String result = "";

		if (!valid()) {
			return result;
		}

		Date dt = BasePoco.timeToLocal(Time);

		String dateDiffStr = (String) DateUtils.getRelativeTimeSpanString(dt.getTime(), now.getTime(), 0);
		return dateDiffStr;
	}
	
	public String toString(Date now) {
		String result = "";

		if (!valid()) {
			return result;
		}

		String dateDiffStr = toRelativeTimeSpanString(now);

		if (Location == null) {
			result = String.format("[%s]", dateDiffStr);
		} else {
			result = String.format("[%s] %s", dateDiffStr, Location);
		}

		return result;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(Time);
		dest.writeString(Location);
		dest.writeString(LocationUrl);
	}

	/**
	 * 
	 */
	public static final Parcelable.Creator<UserActivity> CREATOR = new Creator<UserActivity>() {
		@Override
		public UserActivity[] newArray(int size) {
			return new UserActivity[size];
		}

		@Override
		public UserActivity createFromParcel(Parcel source) {
			return new UserActivity(source);
		}
	};

	/**
	 * 
	 * @param obj
	 * @return
	 * @throws JSONException
	 */
	public static UserActivity fromJson(JSONObject obj) throws JSONException {
		if (!obj.has("activity") || obj.isNull("activity")) {
			return null;
		}

		UserActivity userLocation = new UserActivity();

		JSONObject active = obj.getJSONObject("activity");

		// TODO: cas aktivity
		userLocation.Location = (active.has("location") && !active.isNull("location")) ? active.getString("location") : null;
		userLocation.LocationUrl = (active.has("location_url") && !active.isNull("location_url")) ? active.getString("location_url") : null;

		return userLocation;
	}
}
