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
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class PostFeedTaskWorker extends TaskWorker<FeedQuery, NullResponse> {
		@Override
		public NullResponse doWork(FeedQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class GetFeedDetailTaskWorker extends TaskWorker<FeedQuery, ArrayList<FeedComment>> {
		@Override
		public ArrayList<FeedComment> doWork(FeedQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class PostCommentFeedTaskWorker extends TaskWorker<FeedQuery, NullResponse> {
		@Override
		public NullResponse doWork(FeedQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
