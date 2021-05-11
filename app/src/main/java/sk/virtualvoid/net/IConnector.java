package sk.virtualvoid.net;

import org.json.JSONObject;

import java.util.HashMap;

public interface IConnector {
    String getAuthNick();

    String getAuthToken();

    JSONObjectResult authorizationRequest(String nick);

    JSONObjectResult get(String url);

    IJSONResult getArray(String url);

    JSONObjectResult post(String url);

    JSONObjectResult form(String url, HashMap<String, String> form);

    JSONObjectResult multipart(String url, HashMap<String, Object> parameters);

    JSONObjectResult delete(String url);

    void addOnApiErrorListener(OnApiErrorListener listener);

    void removeOnApiErrorListener(OnApiErrorListener listener);

    interface OnApiErrorListener {
        boolean onError(int httpCode, JSONObject obj);
    }
}
