package sk.virtualvoid.nyxdroid.v2.data;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.virtualvoid.nyxdroid.library.Constants;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * 
 * @author Juraj
 * 
 */
public class Writeup extends BaseComposePoco implements Parcelable {
	public static final int TYPE_DEFAULT = 0;
	public static final int TYPE_MARKET = 97;
	
	private static Pattern ptrImageSearch = Pattern.compile("<img[^>]*src=[\"']([^\"^']*)", Pattern.CASE_INSENSITIVE);
	private static Pattern ptrMarketSearch = Pattern.compile("l=market;l2=(\\d+);id=(\\d+)");
	private static Pattern ptrSpoilerSearch = Pattern.compile("\"spoiler\"", Pattern.CASE_INSENSITIVE);

	public boolean Unread;
	public int Rating;
	public int Type;
	public UserActivity Location;
	public boolean IsSelected;

	public Writeup() {

	}

	public Writeup(Parcel source) {
		Id = source.readLong();
		Nick = source.readString();
		Time = source.readLong();
		Content = source.readString();
		Unread = source.readByte() == 1;
		Rating = source.readInt();
		Type = source.readInt();
		Location = source.readParcelable(UserActivity.class.getClassLoader());
		IsMine = source.readByte() == 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Writeup)) {
			return false;
		}
		
		Writeup other = (Writeup) obj;
		return this.Id == other.Id;
	}

	public ArrayList<Bundle> allImages() {
		Matcher m = ptrImageSearch.matcher(Content);

		ArrayList<Bundle> results = new ArrayList<Bundle>();
		
		while (m.find()) {
			for (int groupIndex = 0; groupIndex < m.groupCount(); groupIndex++) {
				String group = m.group(groupIndex).replace(Constants.WU_IMAGE_PART_SRC, "");
				try {
					String url = group.substring(group.lastIndexOf(Constants.WU_IMAGE_PART_FULLURL) + Constants.WU_IMAGE_PART_FULLURL.length());
					String thumbnailUrl = group.substring(0, group.indexOf(Constants.WU_IMAGE_PART_FULLURL));
					
					Bundle info = new Bundle();
					info.putLong(Constants.KEY_WU_ID, Id);
					info.putString(Constants.KEY_NICK, Nick);
					info.putLong(Constants.KEY_TIME, Time);
					info.putInt(Constants.KEY_RATING, Rating);
					info.putBoolean(Constants.KEY_UNREAD, Unread);
					info.putString(Constants.KEY_URL, URLDecoder.decode(url, Constants.DEFAULT_CHARSET.displayName()));
					info.putString(Constants.KEY_THUMBNAIL_URL, URLDecoder.decode(thumbnailUrl, Constants.DEFAULT_CHARSET.displayName()));
					
					results.add(info);
				} catch (Throwable e) {
					Log.e(Constants.TAG, "allImages, error parsing: " + group);
				}
			}
		}
		
		return results;
	}

	public Long marketId() {
		Matcher m = ptrMarketSearch.matcher(Content);

		if (!m.find() || m.groupCount() == 0) {
			return null;
		}
		
		long id = Long.parseLong(m.group(2));
		return id;
	}
	
	public boolean spoilerPresent() {
		Matcher m = ptrSpoilerSearch.matcher(Content);
		return m.find();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(Id);
		dest.writeString(Nick);
		dest.writeLong(Time);
		dest.writeString(Content);
		dest.writeByte((byte) (Unread ? 1 : 0));
		dest.writeInt(Rating);
		dest.writeInt(Type);
		dest.writeParcelable(Location, 0);
		dest.writeByte((byte) (IsMine ? 1 : 0));
	}

	/**
	 * 
	 */
	public static final Parcelable.Creator<Writeup> CREATOR = new Parcelable.Creator<Writeup>() {
		@Override
		public Writeup[] newArray(int size) {
			return new Writeup[size];
		}

		@Override
		public Writeup createFromParcel(Parcel source) {
			return new Writeup(source);
		}
	};
}
