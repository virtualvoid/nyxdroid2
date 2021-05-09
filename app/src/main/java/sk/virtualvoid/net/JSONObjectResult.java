package sk.virtualvoid.net;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectResult extends JSONResult<JSONObject> {
    protected JSONObjectResult(int statusCode, JSONObject json) {
        super(statusCode, json);
    }

    @Override
    public Error getError() {
        try {
            return Error.fromJSONObject(getJson());
        } catch (JSONException e) {
            throw new RuntimeException("Fucking java...", e);
        }
    }
}
