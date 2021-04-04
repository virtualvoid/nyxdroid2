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
import sk.virtualvoid.nyxdroid.v2.data.Search;
import sk.virtualvoid.nyxdroid.v2.data.UserActivity;
import sk.virtualvoid.nyxdroid.v2.data.UserSearch;
import sk.virtualvoid.nyxdroid.v2.data.query.SearchQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.UserSearchQuery;
import android.app.Activity;

/**
 * 
 * @author Juraj
 * 
 */
public class SearchDataAccess {
	private final static Logger log = Logger.getLogger(SearchDataAccess.class);

	public static Task<SearchQuery, ArrayList<Search>> search(Activity context, TaskListener<ArrayList<Search>> listener) {
		return new Task<SearchQuery, ArrayList<Search>>(context, new SearchTaskWorker(), listener);
	}

	public static Task<UserSearchQuery, ArrayList<UserSearch>> searchUsers(Activity context, TaskListener<ArrayList<UserSearch>> listener) {
		return new Task<UserSearchQuery, ArrayList<UserSearch>>(context, new UserSearchTaskWorker(), listener);
	}

	public static class SearchTaskWorker extends TaskWorker<SearchQuery, ArrayList<Search>> {
		@Override
		public ArrayList<Search> doWork(SearchQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class UserSearchTaskWorker extends TaskWorker<UserSearchQuery, ArrayList<UserSearch>> {
		@Override
		public ArrayList<UserSearch> doWork(UserSearchQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
