package sk.virtualvoid.nyxdroid.v2.data.dac;

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
import sk.virtualvoid.nyxdroid.v2.data.Feed;
import sk.virtualvoid.nyxdroid.v2.data.FeedComment;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.query.FeedQuery;
import android.app.Activity;

/**
 * 
 * @author juraj
 * 
 */
public class FeedDataAccess {
	private final static Logger log = Logger.getLogger(FeedDataAccess.class);

	public static Task<FeedQuery, ArrayList<Feed>> getFeeds(Activity context, TaskListener<ArrayList<Feed>> listener) {
		return new Task<FeedQuery, ArrayList<Feed>>(context, new GetFeedsTaskWorker(), listener);
	}

	public static Task<FeedQuery, NullResponse> postFeed(Activity context, TaskListener<NullResponse> listener) {
		return new Task<FeedQuery, NullResponse>(context, new PostFeedTaskWorker(), listener);
	}

	public static Task<FeedQuery, ArrayList<FeedComment>> getFeedDetails(Activity context, TaskListener<ArrayList<FeedComment>> listener) {
		return new Task<FeedQuery, ArrayList<FeedComment>>(context, new GetFeedDetailTaskWorker(), listener);
	}

	public static Task<FeedQuery, NullResponse> postComment(Activity context, TaskListener<NullResponse> listener) {
		return new Task<FeedQuery, NullResponse>(context, new PostCommentFeedTaskWorker(), listener);
	}

	public static class GetFeedsTaskWorker extends TaskWorker<FeedQuery, ArrayList<Feed>> {
		@Override
		public ArrayList<Feed> doWork(FeedQuery input) throws NyxException {
			ArrayList<Feed> feedList = new ArrayList<Feed>();

			Connector connector = new Connector(getContext());

			JSONObject json = connector.call("feed", "friends", Connector.EmptyParams, this);
			if (json == null || !json.has("data")) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						JSONArray array = json.getJSONArray("data");
						for (int i = 0; i < array.length(); i++) {
							JSONObject obj = array.getJSONObject(i);

							Feed feed = new Feed();

							feed.Id = obj.getLong("id_update");
							feed.Type = obj.getString("type");
							feed.Nick = obj.getString("nick");
							feed.Text = obj.getString("text");
							feed.Time = obj.getLong("time");
							feed.CommentsCount = (obj.has("comments_count") && !obj.isNull("comments_count")) ? obj.getInt("comments_count") : 0;

							feedList.add(feed);
						}
					}
				} catch (JSONException e) {
					log.error("GetFeedsTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return feedList;
		}
	}

	public static class PostFeedTaskWorker extends TaskWorker<FeedQuery, NullResponse> {
		@Override
		public NullResponse doWork(FeedQuery input) throws NyxException {
			NullResponse result = new NullResponse();
			Connector connector = new Connector(getContext());
			try {
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("message", input.Message);

				JSONObject json = connector.call("feed", "send", params, this);
				if (json == null || !json.has("result") || json.isNull("result")) {
					throw new NyxException("Json result was null ?");
				} else {
					String strResult = json.getString("result");
					result.Success = strResult != null && strResult.equalsIgnoreCase(Constants.OK);
				}
			} catch (JSONException e) {
				log.error("PostFeedTaskWorker", e);
				throw new NyxException(e);
			}
			return result;
		}
	}

	public static class GetFeedDetailTaskWorker extends TaskWorker<FeedQuery, ArrayList<FeedComment>> {
		@Override
		public ArrayList<FeedComment> doWork(FeedQuery input) throws NyxException {
			ArrayList<FeedComment> commentList = new ArrayList<FeedComment>();

			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("id", Long.toString(input.Id));
			params.put("user", input.Nick);

			JSONObject json = connector.call("feed", "entry", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				if (json.has("data") && !json.isNull("data")) {
					try {
						JSONObject data = json.getJSONObject("data");
						if (data.has("comments") && !data.isNull("comments")) {
							JSONArray array = data.getJSONArray("comments");
							for (int i = 0; i < array.length(); i++) {
								JSONObject object = array.getJSONObject(i);

								FeedComment comment = new FeedComment();

								comment.Id = (long) i;
								comment.Nick = object.getString("nick");
								comment.Text = object.getString("text");
								comment.Time = object.getLong("time");

								commentList.add(comment);
							}
						}
					} catch (JSONException e) {
						log.error("GetFeedDetailTaskWorker", e);
						throw new NyxException(e);
					}
				}
			}

			return commentList;
		}
	}

	public static class PostCommentFeedTaskWorker extends TaskWorker<FeedQuery, NullResponse> {
		@Override
		public NullResponse doWork(FeedQuery input) throws NyxException {
			NullResponse result = new NullResponse();
			Connector connector = new Connector(getContext());
			try {
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("user", input.Nick);
				params.put("id", Long.toString(input.Id));
				params.put("message", input.Message);

				JSONObject json = connector.call("feed", "send_comment", params, this);
				if (json == null || !json.has("result") || json.isNull("result")) {
					throw new NyxException("Empty result or authorization error.");
				} else {
					String strResult = json.getString("result");
					result.Success = strResult != null && strResult.equalsIgnoreCase(Constants.OK);
				}
			} catch (JSONException e) {
				log.error("PostCommentFeedTaskWorker", e);
				throw new NyxException(e);
			}
			return null;
		}
	}
}
