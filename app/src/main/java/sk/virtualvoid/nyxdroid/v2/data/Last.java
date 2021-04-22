package sk.virtualvoid.nyxdroid.v2.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Last extends Writeup {
    public Long DiscussionId;

    public Last() {
        super(Writeup.TYPE_LAST);
    }

    public Last(Parcel source) {
        super(source);

        DiscussionId = source.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeLong(DiscussionId);
    }

    /**
     *
     */
    public static final Parcelable.Creator<Last> CREATOR = new Parcelable.Creator<Last>() {
        @Override
        public Last[] newArray(int size) {
            return new Last[size];
        }

        @Override
        public Last createFromParcel(Parcel source) {
            return new Last(source);
        }
    };
}
