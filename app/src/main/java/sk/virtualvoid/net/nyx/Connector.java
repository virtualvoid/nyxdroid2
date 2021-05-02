package sk.virtualvoid.net.nyx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.GZipRequestInterceptor;
import sk.virtualvoid.net.GZipResponseInterceptor;
import sk.virtualvoid.net.OverridenSSLSocketFactory;
import sk.virtualvoid.nyxdroid.library.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author juraj
 */
public class Connector {
    private final static Logger log = Logger.getLogger(Connector.class);

    protected Context context = null;
    protected String authNick = null;
    protected String authToken = null;

    public Connector(Context context) {
        if (context == null) {
            log.fatal("Connector ctor got empty context !!!");

            throw new RuntimeException("context");
        }

        this.context = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        authNick = prefs.getString(Constants.AUTH_NICK, null);
        authToken = prefs.getString(Constants.AUTH_TOKEN, null);
    }

    public String getAuthNick() {
        return authNick;
    }

    public String getAuthToken() {
        return authToken;
    }

    public static boolean authorizationRequired(Context context) {
        if (context == null) {
            log.fatal("Connector/authorizationRequired got empty context !!!");

            throw new RuntimeException("context");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String token = prefs.getString(Constants.AUTH_TOKEN, null);
        boolean confirmed = prefs.getBoolean(Constants.AUTH_CONFIRMED, false);
        return !confirmed || token == null;
    }

    public static void authorizationRemove(Context context) {
        if (context == null) {
            log.fatal("Connector/authorizationRemove got empty context !!!");

            throw new RuntimeException("context");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor prefsEditable = prefs.edit();
        prefsEditable.remove(Constants.AUTH_NICK);
        prefsEditable.remove(Constants.AUTH_TOKEN);
        prefsEditable.remove(Constants.AUTH_CONFIRMED);
        prefsEditable.commit();
    }

    public JSONObject authorizationRequest(String nick) {
        String jsonString = "";
        try {
            DefaultHttpClient client = getHttpClient(false);

            HttpPost post = new HttpPost(Constants.getApiUrl() + "/create_token/" + nick);
            HttpResponse response = client.execute(post);
            int statusCode = getStatusCode(response);
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();

                jsonString = convertInputStreamToString(entity.getContent());

                JSONObject obj = new JSONObject(jsonString);
                return obj;
            }
        } catch (Throwable t) {
            log.error(String.format("authorizationRequest=%s", jsonString), t);
        }
        return null;
    }

    public JSONObject get(String url) {
        String jsonString = "";
        try {
            DefaultHttpClient client = getHttpClient(false);

            HttpGet get = new HttpGet(Constants.getApiUrl() + url);
            get.addHeader("Authorization", "Bearer " + authToken);

            HttpResponse response = client.execute(get);
            int statusCode = getStatusCode(response);
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();

                jsonString = convertInputStreamToString(entity.getContent());

                JSONObject obj = new JSONObject(jsonString);
                return obj;
            }
        } catch (Throwable t) {
            log.error(String.format("get=%s", url), t);
        }
        return null;
    }

    public JSONArray getArray(String url) {
        String jsonString = "";
        try {
            DefaultHttpClient client = getHttpClient(false);

            HttpGet get = new HttpGet(Constants.getApiUrl() + url);
            get.addHeader("Authorization", "Bearer " + authToken);

            HttpResponse response = client.execute(get);
            int statusCode = getStatusCode(response);
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();

                jsonString = convertInputStreamToString(entity.getContent());

                JSONArray arr = new JSONArray(jsonString);
                return arr;
            }
        } catch (Throwable t) {
            log.error(String.format("getArray=%s", url), t);
        }
        return null;
    }

    public JSONObject post(String url) {
        String jsonString = "";
        try {
            DefaultHttpClient client = getHttpClient(false);

            HttpPost post = new HttpPost(Constants.getApiUrl() + url);
            post.addHeader("Authorization", "Bearer " + authToken);

            HttpResponse response = client.execute(post);
            int statusCode = getStatusCode(response);
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();

                jsonString = convertInputStreamToString(entity.getContent());

                JSONObject obj = new JSONObject(jsonString);
                return obj;
            }
        } catch (Throwable t) {
            log.error(String.format("post=%s", url), t);
        }
        return null;
    }

    public JSONObject form(String url, List<NameValuePair> form) {
        String jsonString = "";
        try {
            DefaultHttpClient client = getHttpClient(false);

            HttpPost post = new HttpPost(Constants.getApiUrl() + url);
            post.addHeader("Authorization", "Bearer " + authToken);
            post.setEntity(new UrlEncodedFormEntity(form, HTTP.UTF_8));

            HttpResponse response = client.execute(post);
            int statusCode = getStatusCode(response);
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();

                jsonString = convertInputStreamToString(entity.getContent());

                JSONObject obj = new JSONObject(jsonString);
                return obj;
            }
        } catch (Throwable t) {
            log.error(String.format("form=%s", url), t);
        }
        return null;
    }

    public JSONObject multipart(String method, String url, HashMap<String, Object> parameters) {
        String jsonString = "";
        try {
            DefaultHttpClient client = getHttpClient(false);

            HttpEntityEnclosingRequestBase request = null;

            if (method.equalsIgnoreCase("put")) {
                request = new HttpPut(Constants.getApiUrl() + url);
            } else if (method.equalsIgnoreCase("post")) {
                request = new HttpPost(Constants.getApiUrl() + url);
            } else {
                log.error(String.format("multipart: invalid method: %s", method));
                return null;
            }

            request.addHeader("Authorization", "Bearer " + authToken);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            Set<Entry<String, Object>> set = parameters.entrySet();
            Iterator<Entry<String, Object>> it = set.iterator();

            while (it.hasNext()) {
                Entry<String, Object> curr = it.next();

                String key = curr.getKey();
                Object value = curr.getValue();

                if (value instanceof String || value instanceof Long) {
                    String convertedValue = value.toString();
                    builder.addPart(key, new StringBody(convertedValue, ContentType.create("text/plain", Constants.DEFAULT_CHARSET)));
                } else if (value instanceof File) {
                    FileNameMap fileNameMap = URLConnection.getFileNameMap();
                    String contentType = fileNameMap.getContentTypeFor(((File) value).getName());
                    builder.addPart(key, new FileBody((File) value, contentType));
                } else {
                    log.error(String.format("Unknown type for multipart with key: %s", key));
                }
            }

            builder.setMode(HttpMultipartMode.RFC6532);

            request.setEntity(builder.build());

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            int statusCode = getStatusCode(response);
            if (statusCode == 200) {
                jsonString = convertInputStreamToString(entity.getContent());

                JSONObject obj = new JSONObject(jsonString);
                return obj;
            } else {
                jsonString = convertInputStreamToString(entity.getContent());
                JSONObject obj = new JSONObject(jsonString);
                // TODO: check errors
            }
        } catch (Throwable t) {
            log.error(String.format("form=%s", url), t);
        }
        return null;
    }

    public JSONObject delete(String url) {
        String jsonString = "";
        try {
            DefaultHttpClient client = getHttpClient(false);

            HttpDelete get = new HttpDelete(Constants.getApiUrl() + url);
            get.addHeader("Authorization", "Bearer " + authToken);

            HttpResponse response = client.execute(get);
            int statusCode = getStatusCode(response);
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();

                jsonString = convertInputStreamToString(entity.getContent());

                JSONObject obj = new JSONObject(jsonString);
                return obj;
            }
        } catch (Throwable t) {
            log.error(String.format("delete=%s", url), t);
        }
        return null;
    }


    private DefaultHttpClient getHttpClient(boolean includeGzip) {
        DefaultHttpClient client = null;

        HttpParams ps = new BasicHttpParams();
        HttpProtocolParams.setVersion(ps, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(ps, "utf-8");

        String ua = "nyxdroid2; " + android.os.Build.DEVICE + " / " + android.os.Build.MODEL + " / " + android.os.Build.MANUFACTURER + "; " + System.getProperty("os.name") + " " + System.getProperty("os.version");
        HttpProtocolParams.setUserAgent(ps, ua);

        ps.setBooleanParameter("http.protocol.expect-continue", false);
        ps.setParameter("Api-Client-Name", "nyxdroid");

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", new OverridenSSLSocketFactory(), 443));

        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(ps, registry);

        client = new DefaultHttpClient(manager, ps);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));

        if (includeGzip) {
            client.addRequestInterceptor(new GZipRequestInterceptor());
            client.addResponseInterceptor(new GZipResponseInterceptor());
        }

        CookieStore cookieStore = new BasicCookieStore();
        client.setCookieStore(cookieStore);

        /*
        java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
        */

        return client;
    }

    private static int getStatusCode(HttpResponse response) {
        if (response == null || response.getStatusLine() == null) {
            log.warn("getStatusCode the response or status line is null");
            return -1;
        }
        return response.getStatusLine().getStatusCode();
    }

    private static String convertInputStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
    }
}
