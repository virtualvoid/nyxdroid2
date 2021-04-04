package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Friend;
import sk.virtualvoid.nyxdroid.v2.data.UserActivity;
import android.app.Activity;

/**
 * 
 * @author Juraj
 * 
 */
public class FriendDataAccess {
	private final static Logger log = Logger.getLogger(FriendDataAccess.class);
	
	public static Task<ITaskQuery, ArrayList<Friend>> getFriends(Activity context, TaskListener<ArrayList<Friend>> listener) {
		return new Task<ITaskQuery, ArrayList<Friend>>(context, new GetFriendsTaskWorker(), listener);
	}

	public static class GetFriendsTaskWorker extends TaskWorker<ITaskQuery, ArrayList<Friend>> {
		@Override
		public ArrayList<Friend> doWork(ITaskQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
