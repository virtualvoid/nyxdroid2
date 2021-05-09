package sk.virtualvoid.net;

import org.json.JSONException;
import org.json.JSONObject;

public final class Error {
    private String code;
    private String message;

    private Error() {

    }

    private Error(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static Error fromJSONObject(JSONObject obj) throws JSONException {
        if (obj == null || !obj.has("error")) {
            return new Error("general", "Unknown error occurred.");
        }
        Error error = new Error();
        error.code = obj.getString("code");
        error.message = obj.getString("message");
        return error;
    }
}
