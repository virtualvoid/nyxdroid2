package sk.virtualvoid.nyxdroid.v2.data.dac;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.ConnectorFactory;
import sk.virtualvoid.net.Error;
import sk.virtualvoid.net.IConnector;
import sk.virtualvoid.net.IJSONResult;
import sk.virtualvoid.net.JSONArrayResult;
import sk.virtualvoid.net.JSONObjectResult;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Context;
import sk.virtualvoid.nyxdroid.v2.data.Last;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.Poll;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.UserActivity;
import sk.virtualvoid.nyxdroid.v2.data.WaitingFile;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.data.WriteupBookmarkResponse;
import sk.virtualvoid.nyxdroid.v2.data.WriteupHomeResponse;
import sk.virtualvoid.nyxdroid.v2.data.WriteupResponse;
import sk.virtualvoid.nyxdroid.v2.data.query.PollVoteQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupBookmarkQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import sk.virtualvoid.nyxdroid.v2.internal.VotingInfoResult;
import sk.virtualvoid.nyxdroid.v2.internal.VotingResponse;
import sk.virtualvoid.nyxdroid.v2.internal.VotingResult;
import sk.virtualvoid.nyxdroid.v2.internal.WriteupBookmarkQueryType;

/**
 * @author Juraj
 */
public class WriteupDataAccess {
    public static Task<WriteupQuery, SuccessResponse<WriteupResponse>> getWriteups(Activity context, TaskListener<SuccessResponse<WriteupResponse>> listener) {
        return new Task<>(context, new GetWriteupsTaskWorker(), listener);
    }

    public static Task<WriteupQuery, NullResponse> sendWriteup(Activity context, TaskListener<NullResponse> listener) {
        return new Task<>(context, new SendWriteupTaskWorker(), listener);
    }

    public static Task<WriteupQuery, VotingResponse> giveRating(Activity context, TaskListener<VotingResponse> listener) {
        return new Task<>(context, new RateWriteupTaskWorker(), listener);
    }

    public static Task<WriteupQuery, VotingInfoResult> getRatingInfo(Activity context, TaskListener<VotingInfoResult> listener) {
        return new Task<>(context, new RatingOverviewTaskWorker(), listener);
    }

    public static Task<WriteupQuery, NullResponse> reminder(Activity context, TaskListener<NullResponse> listener) {
        return new Task<>(context, new ReminderTaskWorker(), listener);
    }

    public static Task<WriteupQuery, NullResponse> delete(Activity context, TaskListener<NullResponse> listener) {
        return new Task<>(context, new DeleteTaskWorker(), listener);
    }

    public static Task<WriteupQuery, WriteupHomeResponse> getHome(Activity context, TaskListener<WriteupHomeResponse> listener) {
        return new Task<>(context, new GetHomeTaskWorker(), listener);
    }

    public static Task<WriteupBookmarkQuery, WriteupBookmarkResponse> bookOrUnbookWriteup(Activity context, TaskListener<WriteupBookmarkResponse> listener) {
        return new Task<>(context, new BookOrUnbookWriteupTaskWorker(), listener);
    }

    public static Task<ITaskQuery, SuccessResponse<ArrayList<Last>>> getLastWriteups(Activity context, TaskListener<SuccessResponse<ArrayList<Last>>> listener) {
        return new Task<>(context, new GetLastWriteupsTaskWorker(), listener);
    }

    public static Task<PollVoteQuery, Poll> pollVote(Activity context, TaskListener<Poll> listener) {
        return new Task<>(context, new PollVoteTaskWorker(), listener);
    }

    public static class GetWriteupsTaskWorker extends TaskWorker<WriteupQuery, SuccessResponse<WriteupResponse>> {
        @Override
        public SuccessResponse<WriteupResponse> doWork(WriteupQuery input) throws NyxException {
            WriteupResponse result = new WriteupResponse();
            Context context = null;

            IConnector IConnector = ConnectorFactory.getInstance(getContext());

            // older posts (scrolling down)
            String baseUrl = "/discussion/" + input.Id;

            if (input.Direction == Constants.WriteupDirection.WRITEUP_DIRECTION_OLDER && input.LastId != null) {
                baseUrl = baseUrl + "?order=older_than&from_id=" + (input.LastId + 1);
            }

            // replies to particular post
            if (input.Direction == Constants.WriteupDirection.WRITEUP_DIRECTION_NEWER && input.LastId != null) {
                baseUrl = baseUrl + "?order=newer_than&from_id=" + (input.LastId - 1);
                //baseUrl = baseUrl + "/id/" + input.LastId + "/replies";
            }

            if (input.isFilterUser()) {
                if (!baseUrl.contains("?")) {
                    baseUrl = baseUrl + "?";
                } else {
                    baseUrl = baseUrl + "&";
                }
                baseUrl = baseUrl + "user=" + input.FilterUser;
            }
            if (input.isFilterContents()) {
                if (!baseUrl.contains("?")) {
                    baseUrl = baseUrl + "?";
                } else {
                    baseUrl = baseUrl + "&";
                }
                baseUrl = baseUrl + "text=" + input.FilterContents;
            }

            JSONObjectResult api = IConnector.get(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();
                    if (root.has("discussion_common") && !root.isNull("discussion_common")) {
                        JSONObject discussionCommon = root.getJSONObject("discussion_common");
                        JSONObject discussion = discussionCommon.getJSONObject("discussion");

                        result.Id = discussion.getLong("id");
                        result.Name = discussion.getString("name_static");

                        result.CanWrite = discussion.getBoolean("ar_write");
                        result.CanDelete = discussion.getBoolean("ar_delete");

                        if (discussionCommon.has("bookmark") && !discussionCommon.isNull("bookmark")) {
                            JSONObject bookmark = discussionCommon.getJSONObject("bookmark");

                            result.Booked = bookmark.getBoolean("bookmark");
                        }
                    }

                    if (root.has("posts") && !root.isNull("posts")) {
                        result.Writeups = new ArrayList<>();

                        JSONArray posts = root.getJSONArray("posts");

                        for (int postIndex = 0; postIndex < posts.length(); postIndex++) {
                            JSONObject post = posts.getJSONObject(postIndex);

                            Writeup writeup = Writeup.fromJSONObject(post);
                            if (writeup == null) {
                                Log.w(Constants.TAG, "unable to construct writeup, what ?: " + post.toString());
                                continue;
                            }

                            writeup.IsMine = IConnector.getAuthNick().equalsIgnoreCase(post.getString("username"));
                            if (writeup.youtubeFix()) {
                                Log.w(Constants.TAG, String.format("the writeup=%d from discussion=%d contains youtube, fixed.", writeup.Id, result.Id));
                            }

                            result.Writeups.add(writeup);
                        }
                    }

                    context = Context.fromJSONObject(root);
                } catch (Throwable e) {
                    Log.e(Constants.TAG, "GetWriteupsTaskWorker", e);
                    throw new NyxException(e);
                }
            }

            return new SuccessResponse<>(result, context);
        }
    }

    public static class SendWriteupTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
        @Override
        public NullResponse doWork(WriteupQuery input) throws NyxException {
            IConnector connector = ConnectorFactory.getInstance(getContext());

            JSONObjectResult api = null;
            WaitingFile waitingFile = null;

            if (input.AttachmentSource != null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("file", input.AttachmentSource);
                map.put("file_type", "discussion_attachment");
                map.put("id_specific", input.Id);

                api = connector.multipart("/file/upload", map);
                if (!api.isSuccess()) {
                    Error error = api.getError();
                    throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
                }
                waitingFile = WaitingFile.fromJSONObject(api.getJson());
            }

            HashMap<String, String> form = new HashMap<>();
            form.put("content", input.Contents);

            api = connector.form("/discussion/" + input.Id + "/send/text", form);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            }

            return NullResponse.success();
        }
    }

    public static class RateWriteupTaskWorker extends TaskWorker<WriteupQuery, VotingResponse> {
        @Override
        public VotingResponse doWork(WriteupQuery input) throws NyxException {
            VotingResponse result = new VotingResponse();

            IConnector connector = ConnectorFactory.getInstance(getContext());

            String baseUrl = "/discussion/" + input.Id + "/rating/" + input.TempId + "/" + input.VotingType.toString();
            JSONObjectResult api = connector.post(baseUrl);
            if (!api.isSuccess() && !api.isForbidden()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    if (api.isSuccess()) {
                        JSONObject root = api.getJson();
                        result.CurrentRating = root.has("rating") ? root.getInt("rating") : null;
                        result.Result = VotingResult.RATING_CHANGED;
                    } else if (api.isForbidden()) {
                        Error error = api.getError();
                        result.Result = error.getCode().equalsIgnoreCase("NeedsConfirmation")
                                ? VotingResult.RATING_NEEDS_CONFIRMATION : VotingResult.ERROR;
                    }
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "RatingOverviewTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }

    public static class RatingOverviewTaskWorker extends TaskWorker<WriteupQuery, VotingInfoResult> {
        @Override
        public VotingInfoResult doWork(WriteupQuery input) throws NyxException {
            VotingInfoResult result = new VotingInfoResult();

            IConnector connector = ConnectorFactory.getInstance(getContext());

            String baseUrl = "/discussion/" + input.Id + "/rating/" + input.TempId;
            IJSONResult api = connector.getArray(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONArray root = ((JSONArrayResult) api).getJson();
                    for (int voteIndex = 0; voteIndex < root.length(); voteIndex++) {
                        JSONObject vote = root.getJSONObject(voteIndex);

                        String nick = vote.getString("username");
                        String tag = vote.getString("tag");

                        if (tag.equalsIgnoreCase("positive")) {
                            result.Positive++;
                            result.PositiveList.add(nick);
                        } else if (tag.equalsIgnoreCase("negative")) {
                            result.Negative++;
                            result.NegativeList.add(nick);
                        } else {
                            Log.wtf(Constants.TAG, "wtf vote:" + nick + ", " + tag);
                        }
                    }
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "RatingOverviewTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }

    public static class PollVoteTaskWorker extends TaskWorker<PollVoteQuery, Poll> {
        @Override
        public Poll doWork(PollVoteQuery query) throws NyxException {
            Poll result = null;

            IConnector connector = ConnectorFactory.getInstance(getContext());
            String baseUrl = "/discussion/" + query.DiscussionId + "/poll/" + query.WriteupId + "/vote/" + query.Answer;
            JSONObjectResult api = connector.post(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();
                    result = (Poll) Writeup.fromJSONObject(root);
                    result.IsMine = connector.getAuthNick().equalsIgnoreCase(result.Nick);
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "PollVoteTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }

    public static class ReminderTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
        @Override
        public NullResponse doWork(WriteupQuery input) throws NyxException {
            IConnector connector = ConnectorFactory.getInstance(getContext());

            String baseUrl = "/discussion/" + input.Id + "/reminder/" + input.TempId + "/" + input.NewState;
            JSONObjectResult api = connector.post(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            }

            return NullResponse.success();
        }
    }

    public static class DeleteTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
        @Override
        public NullResponse doWork(WriteupQuery input) throws NyxException {
            IConnector connector = ConnectorFactory.getInstance(getContext());

            String baseUrl = "/discussion/" + input.Id + "/delete/" + input.TempId;
            JSONObjectResult api = connector.delete(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            }

            return NullResponse.success();
        }
    }

    public static class GetHomeTaskWorker extends TaskWorker<WriteupQuery, WriteupHomeResponse> {
        @Override
        public WriteupHomeResponse doWork(WriteupQuery input) throws NyxException {
            StringBuilder sb = new StringBuilder();
            WriteupHomeResponse result = new WriteupHomeResponse();

            IConnector connector = ConnectorFactory.getInstance(getContext());

            String baseUrl = "/discussion/" + input.Id;
            JSONObjectResult api = connector.get(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();
                    if (root.has("discussion_common") && root.getJSONObject("discussion_common").has("discussion_specific_data")) {
                        JSONObject discussionCommon = root.getJSONObject("discussion_common");
                        JSONObject discussionSpecific = discussionCommon.getJSONObject("discussion_specific_data");
                        if (discussionSpecific.has("header")) {
                            JSONArray headers = discussionSpecific.getJSONArray("header");
                            for (int headerIndex = 0; headerIndex < headers.length(); headerIndex++) {
                                JSONObject header = headers.getJSONObject(headerIndex);
                                sb.append(header.getString("content"));
                                sb.append("<br/>"); // hehe
                            }
                        }
                    }
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "RatingOverviewTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            result.Header = sb.toString();
            return result;
        }
    }

    public static class BookOrUnbookWriteupTaskWorker extends TaskWorker<WriteupBookmarkQuery, WriteupBookmarkResponse> {
        @Override
        public WriteupBookmarkResponse doWork(WriteupBookmarkQuery input) throws NyxException {
            WriteupBookmarkResponse result = new WriteupBookmarkResponse();

            // /api/discussion/1/bookmark?new_state=true&category=5
            IConnector connector = ConnectorFactory.getInstance(getContext());

            boolean book = input.QueryType == WriteupBookmarkQueryType.BOOK;
            String baseUrl = "/discussion/" + input.DiscussionId + "/bookmark?new_state=" + book;
            if (book) {
                baseUrl += "&category=" + input.CategoryId;
            }

            JSONObjectResult api = connector.get(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            }

            result.Booked = book;
            result.Success = true;

            return result;
        }
    }

    public static class GetLastWriteupsTaskWorker extends TaskWorker<ITaskQuery, SuccessResponse<ArrayList<Last>>> {
        @Override
        public SuccessResponse<ArrayList<Last>> doWork(ITaskQuery iTaskQuery) throws NyxException {
            ArrayList<Last> result = new ArrayList<>();
            Context context = null;

            IConnector connector = ConnectorFactory.getInstance(getContext());

            JSONObjectResult api = connector.get("/last");
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();
                    if (root.has("posts") && !root.isNull("posts")) {
                        JSONArray posts = root.getJSONArray("posts");

                        for (int postIndex = 0; postIndex < posts.length(); postIndex++) {
                            JSONObject post = posts.getJSONObject(postIndex);

                            Last writeup = new Last();
                            writeup.Id = post.getLong("id");
                            writeup.DiscussionId = post.getLong("discussion_id");
                            writeup.DiscussionName = post.getString("discussion_name");

                            writeup.Nick = post.getString("username");
                            writeup.Time = BasePoco.timeFromString(post.getString("inserted_at"));
                            writeup.Content = post.getString("content");
                            writeup.Unread = post.has("new") && post.getBoolean("new");
                            writeup.Rating = post.has("rating") ? post.getInt("rating") : 0;
                            writeup.Type = Writeup.TYPE_DEFAULT;
                            writeup.Location = UserActivity.fromJson(post);
                            writeup.IsMine = connector.getAuthNick().equalsIgnoreCase(post.getString("username"));
                            writeup.CanDelete = post.has("can_be_deleted") && post.getBoolean("can_be_deleted");
                            writeup.IsReminded = post.has("reminder") && post.getBoolean("reminder");

                            if (writeup.youtubeFix()) {
                                Log.w(Constants.TAG, String.format("the writeup=%d from discussion=%d contains youtube, fixed.", writeup.Id, writeup.DiscussionId));
                            }

                            result.add(writeup);
                        }
                    }

                    context = Context.fromJSONObject(root);
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "GetLastWriteupsTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return new SuccessResponse<>(result, context);
        }
    }
}
