package sk.virtualvoid.nyxdroid.v2.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class PollQuestion implements Parcelable {

    public String Key;
    public String Answer;
    public int RespondentsCount;
    public boolean IsMyVote;

    public PollQuestion(String key) {
        Key = key;
    }

    protected PollQuestion(Parcel source) {
        Key = source.readString();
        Answer = source.readString();
        RespondentsCount = source.readInt();
        IsMyVote = source.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Key);
        dest.writeString(Answer);
        dest.writeInt(RespondentsCount);
        dest.writeByte((byte) (IsMyVote ? 1 : 0));
    }

    public static PollQuestion fromJSONObject(String key, JSONObject obj) throws JSONException {
        PollQuestion question = new PollQuestion(key);

        question.Answer = obj.getString("answer");

        if (obj.has("result") && !obj.isNull("result")) {
            JSONObject meta = obj.getJSONObject("result");

            question.RespondentsCount = meta.getInt("respondents_count");
            question.IsMyVote = meta.has("is_my_vote") && meta.getBoolean("is_my_vote");
        }

        return question;
    }

    public static final Creator<PollQuestion> CREATOR = new Creator<PollQuestion>() {
        @Override
        public PollQuestion createFromParcel(Parcel in) {
            return new PollQuestion(in);
        }

        @Override
        public PollQuestion[] newArray(int size) {
            return new PollQuestion[size];
        }
    };
}
