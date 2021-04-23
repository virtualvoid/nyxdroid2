package sk.virtualvoid.nyxdroid.v2.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Poll extends Writeup {

    public String Question;
    public String Instructions;
    public boolean PublicResults;
    public int AllowedVotes;
    public List<PollAnswer> Answers;
    public boolean CanModify;
    public boolean UserDidVote;
    public int TotalVotes;
    public int TotalRespondents;
    public int MaximumAnswerVotes;

    public Poll() {
        super(Writeup.TYPE_POLL);

        Answers = new ArrayList<>();
    }

    public Poll(Parcel source) {
        super(source);

        Question = source.readString();
        Instructions = source.readString();
        PublicResults = source.readByte() == 1;
        AllowedVotes = source.readInt();
        CanModify = source.readByte() == 1;
        UserDidVote = source.readByte() == 1;
        TotalVotes = source.readInt();
        TotalRespondents = source.readInt();
        MaximumAnswerVotes = source.readInt();

        Answers = source.readArrayList(PollAnswer.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(Question);
        dest.writeString(Instructions);
        dest.writeByte((byte)(PublicResults ? 1 : 0));
        dest.writeInt(AllowedVotes);
        dest.writeByte((byte)(CanModify ? 1 : 0));
        dest.writeByte((byte)(UserDidVote ? 1 : 0));
        dest.writeInt(TotalVotes);
        dest.writeInt(TotalRespondents);
        dest.writeInt(MaximumAnswerVotes);
        dest.writeList(Answers);
    }

    public static Poll fromJSONObject(JSONObject post) throws JSONException {
        Poll poll = new Poll();

        poll.Question = post.getString("question");
        poll.Instructions = post.has("instructions") ? post.getString("instructions") : "";
        poll.PublicResults = post.getBoolean("public_results");
        poll.AllowedVotes = post.getInt("allowed_votes");

        JSONObject answers = post.getJSONObject("answers");

        Iterator<String> it = answers.keys();
        while(it.hasNext()) {
            String key = it.next();
            JSONObject answer = answers.getJSONObject(key);
            poll.Answers.add(PollAnswer.fromJSONObject(key, answer));
        }

        if (post.has("computed_values") && !post.isNull("computed_values")) {
            JSONObject computedValues = post.getJSONObject("computed_values");

            poll.CanModify = computedValues.has("can_modify") && computedValues.getBoolean("can_modify");
            poll.UserDidVote = computedValues.has("user_did_vote") && computedValues.getBoolean("user_did_vote");
            poll.TotalVotes = computedValues.has("total_votes") ? computedValues.getInt("total_votes") : 0;
            poll.TotalRespondents = computedValues.has("total_respondents") ? computedValues.getInt("total_respondents") : 0;
            poll.MaximumAnswerVotes = computedValues.has("maximum_answer_votes") ? computedValues.getInt("maximum_answer_votes") : 0;
        }

        return poll;
    }

    /**
     *
     */
    public static final Parcelable.Creator<Poll> CREATOR = new Parcelable.Creator<Poll>() {
        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }

        @Override
        public Poll createFromParcel(Parcel source) {
            return new Poll(source);
        }
    };
}
