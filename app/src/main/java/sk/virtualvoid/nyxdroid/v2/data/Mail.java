package sk.virtualvoid.nyxdroid.v2.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author juraj
 * 
 */
public class Mail extends BaseComposePoco implements Parcelable {
	public String Direction;
	public boolean IsUnread;
	public UserActivity Location;
	
	public Mail() {

	}

	private Mail(Parcel source) {
		Id = source.readLong();
		Nick = source.readString();
		Direction = source.readString();
		Content = source.readString();
		Time = source.readLong();
		IsUnread = source.readByte() == 1;
		Location = source.readParcelable(UserActivity.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(Id);
		dest.writeString(Nick);
		dest.writeString(Direction);
		dest.writeString(Content);
		dest.writeLong(Time);
		dest.writeByte((byte) (IsUnread ? 1 : 0));
		dest.writeParcelable(Location, 0);
	}

	/**
	 * 
	 */
	public static final Parcelable.Creator<Mail> CREATOR = new Creator<Mail>() {
		@Override
		public Mail[] newArray(int size) {
			return new Mail[size];
		}

		@Override
		public Mail createFromParcel(Parcel source) {
			return new Mail(source);
		}
	};
}
