package sk.virtualvoid.net;

import org.json.JSONException;
import org.json.JSONObject;

public final class Error {
    public static final String GENERAL = "general";
    public static final String UNKNOWN_ERROR_OCCURRED = "Unknown error occurred.";

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
            return new Error(GENERAL, UNKNOWN_ERROR_OCCURRED);
        }
        Error error = new Error();
        error.code = obj.has("code") ? obj.getString("code") : GENERAL;
        error.message = obj.has("message") ?  obj.getString("message") : UNKNOWN_ERROR_OCCURRED;
        return error;
    }
}
