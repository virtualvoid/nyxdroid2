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
			ArrayList<Search> results = new ArrayList<Search>();

			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			if (input.Nick != null && input.Nick.length() > 0) {
				params.put("filter_user", input.Nick);
			}

			if (input.Phrase != null && input.Phrase.length() > 0) {
				params.put("filter_text", input.Phrase);
			}

			params.put("page", Integer.toString(input.Position));

			JSONObject json = connector.call("search", "writeups", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						JSONArray array = json.getJSONArray("data");
						for (int i = 0; i < array.length(); i++) {
							JSONObject object = array.getJSONObject(i);

							Search found = new Search();
							found.Id = object.getLong("id_wu");
							found.DiscussionId = object.getLong("id_klub");
							found.DiscussionName = object.getString("klub_jmeno");
							found.Content = object.getString("content");
							found.Nick = object.getString("nick");
							found.Time = object.getLong("time");
							found.Rating = object.getInt("wu_rating");

							results.add(found);
						}
					}
				} catch (JSONException e) {
					log.error("SearchTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return results;
		}
	}

	public static class UserSearchTaskWorker extends TaskWorker<UserSearchQuery, ArrayList<UserSearch>> {

		private static void parse(JSONArray source, ArrayList<UserSearch> target) throws JSONException {
			for (int i = 0; i < source.length(); i++) {
				JSONObject json = source.getJSONObject(i);

				UserSearch userSearch = new UserSearch();
				userSearch.Nick = json.getString("nick");
				userSearch.Location = UserActivity.fromJson(json);

				target.add(userSearch);
			}
		}

		@Override
		public ArrayList<UserSearch> doWork(UserSearchQuery input) throws NyxException {
			ArrayList<UserSearch> results = new ArrayList<UserSearch>();

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("nick", input.Nick);

			Connector connector = new Connector(getContext());
			JSONObject json = connector.call("people", "autocomplete", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						JSONObject data = json.getJSONObject("data");

						String[] fields = new String[] { "exact", "friends", "others" };
						for (String field : fields) {
							if (!data.isNull(field)) {
								parse(data.getJSONArray(field), results);
							}
						}
					}
				} catch (JSONException e) {
					log.error("UserSearchTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return results;
		}
	}
}
