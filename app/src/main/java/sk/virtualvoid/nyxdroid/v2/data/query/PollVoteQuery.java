package sk.virtualvoid.nyxdroid.v2.data.query;

import sk.virtualvoid.core.ITaskQuery;

public class PollVoteQuery implements ITaskQuery {
    public int AdapterPosition;

    public long DiscussionId;
    public long WriteupId;
    public String Answer;
}
