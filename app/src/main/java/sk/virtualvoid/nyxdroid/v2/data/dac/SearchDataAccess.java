package sk.virtualvoid.nyxdroid.v2.data.dac;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.ConnectorFactory;
import sk.virtualvoid.net.Error;
import sk.virtualvoid.net.IConnector;
import sk.virtualvoid.net.JSONObjectResult;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Search;
import sk.virtualvoid.nyxdroid.v2.data.UserSearch;
import sk.virtualvoid.nyxdroid.v2.data.query.SearchQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.UserSearchQuery;

/**
 * @author Juraj
 */
public class SearchDataAccess {
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
            ArrayList<UserSearch> result = new ArrayList<>();

            IConnector connector = ConnectorFactory.getInstance(getContext());

            JSONObjectResult api = connector.get("/search/username/" + input.Nick);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();

                    result.addAll(getUsers(root, "exact"));
                    result.addAll(getUsers(root, "friends"));
                    result.addAll(getUsers(root, "others"));
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "UserSearchTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }

    public static ArrayList<UserSearch> getUsers(JSONObject root, String category) throws Throwable {
        ArrayList<UserSearch> result = new ArrayList<>();
        if (root.has(category) && !root.isNull(category)) {
            JSONArray others = root.getJSONArray(category);

            for (int userIndex = 0; userIndex < others.length(); userIndex++) {
                JSONObject user = others.getJSONObject(userIndex);

                String nick = user.getString("username");

                result.add(new UserSearch(nick));
            }
        }
        return result;
    }
}
