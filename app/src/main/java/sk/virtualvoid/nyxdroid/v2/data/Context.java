package sk.virtualvoid.nyxdroid.v2.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Context {
    public class User {
        private String name;
        private int unreadMail;
        private String lastMailFrom;
        private int unreadNotifications;

        private User() {

        }

        public int getUnreadMail() {
            return unreadMail;
        }
    }

    private User user;

    private Context() {
        user = new User();
    }

    public User getUser() {
        return user;
    }

    public static Context fromJSONObject(JSONObject root) throws JSONException {
        if (!root.has("context") || root.isNull("context")) {
            return null;
        }

        JSONObject context = root.getJSONObject("context");
        if (!context.has("user") || context.isNull("user")) {
            return null;
        }

        JSONObject user = context.getJSONObject("user");

        Context result = new Context();
        result.user.name = user.getString("username");
        result.user.unreadMail = user.getInt("mail_unread");
        result.user.lastMailFrom = user.getString("mail_last_from");
        result.user.unreadNotifications = user.getInt("notifications_unread");

        return result;
    }
}
