package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.BaseResponse;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.UserActivity;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.data.WriteupBookmarkResponse;
import sk.virtualvoid.nyxdroid.v2.data.WriteupHomeResponse;
import sk.virtualvoid.nyxdroid.v2.data.WriteupResponse;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupBookmarkQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import sk.virtualvoid.nyxdroid.v2.internal.VotingInfoResult;
import sk.virtualvoid.nyxdroid.v2.internal.VotingResponse;
import sk.virtualvoid.nyxdroid.v2.internal.VotingResult;
import sk.virtualvoid.nyxdroid.v2.internal.WriteupBookmarkQueryType;

import android.app.Activity;

/**
 * @author Juraj
 */
public class WriteupDataAccess {
    private final static Logger log = Logger.getLogger(WriteupDataAccess.class);

    public static Task<WriteupQuery, WriteupResponse> getWriteups(Activity context, TaskListener<WriteupResponse> listener) {
        return new Task<WriteupQuery, WriteupResponse>(context, new GetWriteupsTaskWorker(), listener);
    }

    public static Task<WriteupQuery, NullResponse> sendWriteup(Activity context, TaskListener<NullResponse> listener) {
        return new Task<WriteupQuery, NullResponse>(context, new SendWriteupTaskWorker(), listener);
    }

    public static Task<WriteupQuery, VotingResponse> giveRating(Activity context, TaskListener<VotingResponse> listener) {
        return new Task<WriteupQuery, VotingResponse>(context, new RateWriteupTaskWorker(), listener);
    }

    public static Task<WriteupQuery, VotingInfoResult> getRatingInfo(Activity context, TaskListener<VotingInfoResult> listener) {
        return new Task<WriteupQuery, VotingInfoResult>(context, new RatingOverviewTaskWorker(), listener);
    }

    public static Task<WriteupQuery, NullResponse> reminder(Activity context, TaskListener<NullResponse> listener) {
        return new Task<WriteupQuery, NullResponse>(context, new ReminderTaskWorker(), listener);
    }

    public static Task<WriteupQuery, NullResponse> delete(Activity context, TaskListener<NullResponse> listener) {
        return new Task<WriteupQuery, NullResponse>(context, new DeleteTaskWorker(), listener);
    }

    public static Task<WriteupQuery, WriteupHomeResponse> getHome(Activity context, TaskListener<WriteupHomeResponse> listener) {
        return new Task<WriteupQuery, WriteupHomeResponse>(context, new GetHomeTaskWorker(), listener);
    }

    public static Task<WriteupBookmarkQuery, WriteupBookmarkResponse> bookOrUnbookWriteup(Activity context, TaskListener<WriteupBookmarkResponse> listener) {
        return new Task<WriteupBookmarkQuery, WriteupBookmarkResponse>(context, new BookOrUnbookWriteupTaskWorker(), listener);
    }

    public static class GetWriteupsTaskWorker extends TaskWorker<WriteupQuery, WriteupResponse> {
        @Override
        public WriteupResponse doWork(WriteupQuery input) throws NyxException {
            WriteupResponse result = new WriteupResponse();

            Connector connector = new Connector(getContext());

            String baseUrl = "/discussion/" + input.Id;
            if (input.Direction == Constants.WriteupDirection.WRITEUP_DIRECTION_OLDER && input.LastId != null) {
                baseUrl = baseUrl + "?order=older_than&from_id=" + input.LastId;
            }

            if (input.isFilterUser()) {
                if (!baseUrl.contains("?")) {
                    baseUrl = baseUrl + "?";
                } else {
                    baseUrl = baseUrl + "&";
                }
                baseUrl =baseUrl + "user="+input.FilterUser;
            }
            if (input.isFilterContents()) {
                if (!baseUrl.contains("?")) {
                    baseUrl = baseUrl + "?";
                } else {
                    baseUrl = baseUrl + "&";
                }
                baseUrl =baseUrl + "text="+input.FilterContents;
            }

            JSONObject root = connector.get(baseUrl);
            if (root == null) {
                throw new NyxException("Json result was null ?");
            } else {
                try {
                    if (root.has("discussion_common")) {
                        JSONObject discussionCommon = root.getJSONObject("discussion_common");
                        JSONObject discussion = discussionCommon.getJSONObject("discussion");

                        result.Id = discussion.getLong("id");
                        result.Name = discussion.getString("name_static");

                        result.CanWrite = discussion.getBoolean("ar_write");
                        result.CanDelete = discussion.getBoolean("ar_delete");

                        if (discussionCommon.has("bookmark")) {
                            JSONObject bookmark = discussionCommon.getJSONObject("bookmark");

                            result.Booked = bookmark.getBoolean("bookmark");
                        }
                    }

                    if (root.has("posts")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                        result.Writeups = new ArrayList<>();

                        JSONArray posts = root.getJSONArray("posts");

                        for (int postIndex = 0; postIndex < posts.length(); postIndex++) {
                            JSONObject post = posts.getJSONObject(postIndex);

                            Writeup writeup = new Writeup();
                            writeup.Id = post.getLong("id");
                            writeup.Nick = post.getString("username");

                            writeup.Time = sdf.parse(post.getString("inserted_at")).getTime();

                            writeup.Content = post.getString("content");
                            writeup.Unread = post.has("new") && post.getBoolean("new");
                            writeup.Rating = post.has("rating") ? post.getInt("rating") : 0;
                            writeup.Type = Writeup.TYPE_DEFAULT; // TODO: ked sa dorobi market tak checknut o co ide
                            writeup.Location = UserActivity.fromJson(post);
                            writeup.IsMine = connector.getAuthNick()
                                    .equalsIgnoreCase(post.getString("username"));

                            result.Writeups.add(writeup);
                        }
                    }
                } catch (Throwable e) {
                    log.error("GetWriteupsTaskWorker", e);
                    throw new NyxException(e);
                }
            }

            return result;
        }
    }

    public static class SendWriteupTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
        @Override
        public NullResponse doWork(WriteupQuery input) throws NyxException {
            Connector connector = new Connector(getContext());

            String baseUrl = "/discussion/" + input.Id + "/send/text";

            JSONObject json = connector.form(baseUrl, input.Contents);

            return new NullResponse();
        }
    }

    public static class RateWriteupTaskWorker extends TaskWorker<WriteupQuery, VotingResponse> {
        @Override
        public VotingResponse doWork(WriteupQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }

    public static class RatingOverviewTaskWorker extends TaskWorker<WriteupQuery, VotingInfoResult> {
        @Override
        public VotingInfoResult doWork(WriteupQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }

    public static class ReminderTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
        @Override
        public NullResponse doWork(WriteupQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }

    public static class DeleteTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
        @Override
        public NullResponse doWork(WriteupQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }

    public static class GetHomeTaskWorker extends TaskWorker<WriteupQuery, WriteupHomeResponse> {
        @Override
        public WriteupHomeResponse doWork(WriteupQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }

    public static class BookOrUnbookWriteupTaskWorker extends TaskWorker<WriteupBookmarkQuery, WriteupBookmarkResponse> {
        @Override
        public WriteupBookmarkResponse doWork(WriteupBookmarkQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }
}
