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
			ArrayList<Friend> results = new ArrayList<Friend>();

			Connector connector = new Connector(getContext());

			JSONObject json = connector.call("people", "active_friends", Connector.EmptyParams, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						JSONArray array = json.getJSONArray("data");
						for (int i = 0; i < array.length(); i++) {
							JSONObject obj = array.getJSONObject(i);

							Friend friend = new Friend();
							friend.Id = (long) i;
							friend.Nick = obj.getString("nick");
							friend.Location = UserActivity.fromJson(obj);

							results.add(friend);
						}
					}
				} catch (JSONException e) {
					log.error("GetFriendsTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return results;
		}
	}
}
