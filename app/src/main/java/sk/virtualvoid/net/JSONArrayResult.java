package sk.virtualvoid.net;

import org.json.JSONArray;

public class JSONArrayResult extends JSONResult<JSONArray> {
    protected JSONArrayResult(int statusCode, JSONArray jsonArray) {
        super(statusCode, jsonArray);
    }

    @Override
    public Error getError() {
        throw new RuntimeException("Not Supported");
    }
}
