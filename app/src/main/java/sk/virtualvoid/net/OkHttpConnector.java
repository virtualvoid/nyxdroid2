package sk.virtualvoid.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import sk.virtualvoid.nyxdroid.library.Constants;

public class OkHttpConnector implements IConnector {
    private static final Logger log = Logger.getLogger(OkHttpConnector.class);
    private static final MediaType applicationJson = MediaType.get("application/json; charset=utf-8");
    private static final FileNameMap fileNameMap = URLConnection.getFileNameMap();

    protected Context context = null;
    protected String authNick = null;
    protected String authToken = null;

    protected ArrayList<OnApiErrorListener> onApiErrorListeners;

    OkHttpConnector(Context context) {
        if (context == null) {
            log.fatal("Connector ctor got empty context !!!");

            throw new RuntimeException("context");
        }

        this.context = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        authNick = prefs.getString(Constants.AUTH_NICK, null);
        authToken = prefs.getString(Constants.AUTH_TOKEN, null);

        onApiErrorListeners = new ArrayList<>();
        onApiErrorListeners.add(new OnApiErrorListener() {
            @Override
            public boolean onError(int httpCode, JSONObject obj) {
                log.error(String.format("HTTP: %d, RESPONSE: %s", httpCode, obj.toString()));
                return false;
            }
        });
    }

    @Override
    public String getAuthNick() {
        return authNick;
    }

    @Override
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public JSONObjectResult authorizationRequest(String nick) {
        try {
            OkHttpClient client = getHttpClient();

            Request request = new Request.Builder()
                    .post(RequestBody.create("", applicationJson))
                    .url(String.format("%s/create_token/%s", Constants.getApiUrl(), nick))
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            int code = response.code();
            JSONObject json = new JSONObject(body.string());

            if (!response.isSuccessful()) {
                notifyOnApiErrorListeners(code, json);
            }

            return new JSONObjectResult(code, json);
        } catch (Throwable t) {
            log.error(String.format("authorizationRequest=%s: %s", nick, t.getMessage()));
        }

        return null;
    }

    @Override
    public JSONObjectResult get(String url) {
        try {
            OkHttpClient client = getHttpClient();

            Request request = new Request.Builder()
                    .url(String.format("%s/%s", Constants.getApiUrl(), url))
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            int code = response.code();
            JSONObject json = new JSONObject(body.string());

            if (!response.isSuccessful()) {
                notifyOnApiErrorListeners(code, json);
            }

            return new JSONObjectResult(code, json);
        } catch (Throwable t) {
            log.error(String.format("get=%s: %s", url, t.getMessage()));
        }

        return null;
    }

    @Override
    public IJSONResult getArray(String url) {
        try {
            OkHttpClient client = getHttpClient();

            Request request = new Request.Builder()
                    .url(String.format("%s/%s", Constants.getApiUrl(), url))
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            int code = response.code();

            if (response.isSuccessful()) {
                JSONArray arr = new JSONArray(body.string());
                return new JSONArrayResult(code, arr);
            } else {
                JSONObject json = new JSONObject(body.string());
                notifyOnApiErrorListeners(code, json);
                return new JSONObjectResult(code, json);
            }
        } catch (Throwable t) {
            log.error(String.format("getArray=%s: %s", url, t.getMessage()));
        }

        return null;
    }

    @Override
    public JSONObjectResult post(String url) {
        try {
            OkHttpClient client = getHttpClient();

            Request request = new Request.Builder()
                    .post(RequestBody.create("", applicationJson))
                    .url(String.format("%s/%s", Constants.getApiUrl(), url))
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            int code = response.code();
            JSONObject json = new JSONObject(body.string());

            if (!response.isSuccessful()) {
                notifyOnApiErrorListeners(code, json);
            }

            return new JSONObjectResult(code, json);
        } catch (Throwable t) {
            log.error(String.format("post=%s: %s", url, t.getMessage()));
        }

        return null;
    }

    @Override
    public JSONObjectResult form(String url, HashMap<String, String> pairs) {
        try {
            OkHttpClient client = getHttpClient();

            FormBody.Builder formBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> pair : pairs.entrySet()) {
                formBuilder.add(pair.getKey(), pair.getValue());
            }

            RequestBody form = formBuilder.build();

            Request request = new Request.Builder()
                    .post(form)
                    .url(String.format("%s/%s", Constants.getApiUrl(), url))
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            int code = response.code();
            JSONObject json = new JSONObject(body.string());

            if (!response.isSuccessful()) {
                notifyOnApiErrorListeners(code, json);
            }

            return new JSONObjectResult(code, json);
        } catch (Throwable t) {
            log.error(String.format("form=%s: %s", url, t.getMessage()));
        }

        return null;
    }

    @Override
    public JSONObjectResult multipart(String url, HashMap<String, Object> pairs) {
        try {
            OkHttpClient client = getHttpClient();

            MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            for (Map.Entry<String, Object> pair : pairs.entrySet()) {
                String key = pair.getKey();
                Object value = pair.getValue();

                if (value instanceof String || value instanceof Long) {
                    String convertedValue = value.toString();
                    requestBodyBuilder.addFormDataPart(key, convertedValue);
                } else if (value instanceof File) {
                    File file = (File)value;
                    String contentType = fileNameMap.getContentTypeFor(file.getName());
                    requestBodyBuilder.addFormDataPart(key, file.getName(), RequestBody.create(file, MediaType.parse(contentType)));
                } else {
                    log.error(String.format("Unknown type for multipart with key: %s", key));
                }
            }

            RequestBody requestBody = requestBodyBuilder.build();

            Request request = new Request.Builder()
                    .put(requestBody)
                    .url(String.format("%s/%s", Constants.getApiUrl(), url))
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            int code = response.code();
            JSONObject json = new JSONObject(body.string());

            if (!response.isSuccessful()) {
                notifyOnApiErrorListeners(code, json);
            }

            return new JSONObjectResult(code, json);
        } catch (Throwable t) {
            log.error(String.format("multipart=%s: %s", url, t.getMessage()));
        }

        return null;
    }

    @Override
    public JSONObjectResult delete(String url) {
        try {
            OkHttpClient client = getHttpClient();

            Request request = new Request.Builder()
                    .delete()
                    .url(String.format("%s/%s", Constants.getApiUrl(), url))
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            int code = response.code();
            JSONObject json = new JSONObject(body.string());

            if (!response.isSuccessful()) {
                notifyOnApiErrorListeners(code, json);
            }

            return new JSONObjectResult(code, json);
        } catch (Throwable t) {
            log.error(String.format("delete=%s: %s", url, t.getMessage()));
        }

        return null;
    }

    @Override
    public void addOnApiErrorListener(OnApiErrorListener listener) {
        onApiErrorListeners.add(listener);
    }

    @Override
    public void removeOnApiErrorListener(OnApiErrorListener listener) {
        onApiErrorListeners.remove(listener);
    }

    private void notifyOnApiErrorListeners(int httpCode, JSONObject obj) {
        for (int listenerIndex = 0; listenerIndex < onApiErrorListeners.size(); listenerIndex++) {
            OnApiErrorListener listener = onApiErrorListeners.get(listenerIndex);
            if (listener == null) {
                continue;
            }

            listener.onError(httpCode, obj);
        }
    }

    private OkHttpClient getHttpClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor(new UserAgentInterceptor())
                .addInterceptor(new AuthorizationInterceptor(getAuthNick(), getAuthToken()))
                .cache(null)
                .build();

        return client;
    }

    private static final class UserAgentInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request.Builder builder = chain
                    .request()
                    .newBuilder();

            String userAgent = "nyxdroid2; " + android.os.Build.DEVICE + " / " + android.os.Build.MODEL + " / " + android.os.Build.MANUFACTURER + "; " + System.getProperty("os.name") + " " + System.getProperty("os.version");
            builder.addHeader("User-Agent", userAgent);

            Request request = builder.build();
            return chain.proceed(request);
        }
    }

    private static final class AuthorizationInterceptor implements Interceptor {
        private final String authNick;
        private final String authToken;

        public AuthorizationInterceptor(String authNick, String authToken) {
            this.authNick = authNick;
            this.authToken = authToken;
        }

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request.Builder builder = chain
                    .request()
                    .newBuilder();

            if (authNick != null && !authNick.isEmpty() && authToken != null && !authToken.isEmpty()) {
                builder.addHeader("Authorization-Context", String.format("%s %s", Constants.INDEX_WWW, authNick.toUpperCase()));
                builder.addHeader("Authorization", String.format("Bearer %s", authToken));
            }

            Request request = builder.build();
            return chain.proceed(request);
        }
    }
}
