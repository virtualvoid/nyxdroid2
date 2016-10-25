package sk.virtualvoid.net.nyx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
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
 * 
 * @author juraj
 * 
 */
public class Connector {
	private final static Logger log = Logger.getLogger(Connector.class);

	protected Context context = null;
	protected String authNick = null;
	protected String authToken = null;
	protected String authCode = null;
	protected boolean sslEnabled = false;
	protected boolean overrideSsl = false;
	protected boolean useBetaApi = false;

	public static final HashMap<String, Object> EmptyParams = new HashMap<String, Object>();

	public Connector(Context context) {
		if (context == null) {
			log.fatal("Connector ctor got empty context !!!");

			throw new RuntimeException("context");
		}

		this.context = context;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		authNick = prefs.getString(Constants.AUTH_NICK, null);
		authToken = prefs.getString(Constants.AUTH_TOKEN, null);
		authCode = prefs.getString(Constants.AUTH_CODE, null);
		sslEnabled = prefs.getBoolean(Constants.SETTINGS_SSL_ENABLED, true);
		overrideSsl = prefs.getBoolean(Constants.SETTINGS_SSL_OVERRIDE, false);
		useBetaApi = prefs.getBoolean(Constants.SETTINGS_USE_BETA_API, false);
	}

	public String getAuthNick() {
		return authNick;
	}

	@Deprecated
	public String getAuthToken() {
		return null;
	}

	@Deprecated
	public String getAuthCode() {
		return null;
	}

	public static boolean authorizationRequired(Context context) {
		if (context == null) {
			log.fatal("Connector/authorizationRequired got empty context !!!");

			throw new RuntimeException("context");
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String nick = prefs.getString(Constants.AUTH_NICK, null);
		String token = prefs.getString(Constants.AUTH_TOKEN, null);
		String code = prefs.getString(Constants.AUTH_CODE, null);
		boolean confirmed = prefs.getBoolean(Constants.AUTH_CONFIRMED, false);
		return !confirmed || (nick == null && code == null && token == null);
	}

	public static void authorizationRemove(Context context) {
		if (context == null) {
			log.fatal("Connector/authorizationRemove got empty context !!!");

			throw new RuntimeException("context");
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		SharedPreferences.Editor prefsEditable = prefs.edit();
		prefsEditable.remove(Constants.AUTH_NICK);
		prefsEditable.remove(Constants.AUTH_CODE);
		prefsEditable.remove(Constants.AUTH_TOKEN);
		prefsEditable.remove(Constants.AUTH_CONFIRMED);
		prefsEditable.commit();
	}

	public JSONObject authorizationRequest(String nick) {
		String jsonString = "";
		try {
			DefaultHttpClient client = getHttpClient(false);

			HttpPost post = new HttpPost(Constants.getApiUrl(false, false));
			List<NameValuePair> form = new ArrayList<NameValuePair>();

			form.add(new BasicNameValuePair("auth_nick", nick));
			form.add(new BasicNameValuePair("auth_token", ""));

			form.add(new BasicNameValuePair("l", "help"));
			form.add(new BasicNameValuePair("l2", "test"));

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
			log.error(String.format("authorizationRequest=%s", jsonString), t);
		}
		return null;
	}

	private HttpEntity buildMultipartEntity(String l, String l2, HashMap<String, Object> other) throws UnsupportedEncodingException {
		MultipartEntityBuilder postEntityBuilder = MultipartEntityBuilder.create();

		postEntityBuilder.addTextBody("auth_nick", authNick);
		postEntityBuilder.addTextBody("auth_token", authToken);
		postEntityBuilder.addTextBody("l", l);
		postEntityBuilder.addTextBody("l2", l2);

		Set<Entry<String, Object>> set = other.entrySet();
		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> curr = (Entry<String, Object>) it.next();

			String key = curr.getKey();
			Object value = curr.getValue();

			if (value instanceof String) {
				postEntityBuilder.addPart(key, new StringBody((String) value, ContentType.create("text/plain", Constants.DEFAULT_CHARSET)));
			} else if (value instanceof File) {
				postEntityBuilder.addPart(key, new FileBody((File) value));
			} else {
				log.error(String.format("Unknown type for http call with key: %s", key));
			}
		}

		//postEntityBuilder.setCharset(Constants.DEFAULT_CHARSET);

		return postEntityBuilder.build();
	}

	private HttpEntity buildUrlEncodedEntity(String l, String l2, HashMap<String, Object> other) throws UnsupportedEncodingException {
		List<NameValuePair> form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("auth_nick", authNick));
		form.add(new BasicNameValuePair("auth_token", authToken));
		form.add(new BasicNameValuePair("l", l));
		form.add(new BasicNameValuePair("l2", l2));

		Set<Entry<String, Object>> set = other.entrySet();
		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> curr = (Entry<String, Object>) it.next();
			form.add(new BasicNameValuePair(curr.getKey(), (String) curr.getValue()));
		}

		return new UrlEncodedFormEntity(form, HTTP.UTF_8);
	}

	public JSONObject call(String l, String l2, HashMap<String, Object> other, TaskWorker<?, ?> taskWorker) {
		String jsonString = "";
		try {
			DefaultHttpClient client = getHttpClient(true);

			HttpPost post = new HttpPost(Constants.getApiUrl(sslEnabled, useBetaApi));

			HttpEntity postEntity = null;

			if (isMultipart(other)) {
				postEntity = buildMultipartEntity(l, l2, other);
			} else {
				postEntity = buildUrlEncodedEntity(l, l2, other);
			}

			post.setEntity(postEntity);

			HttpResponse response = client.execute(post);

			int statusCode = getStatusCode(response);
			if (statusCode != HttpStatus.SC_OK) {
				log.warn(String.format("call %s / %s STATUS CODE = %d", l, l2, statusCode));
			}

			HttpEntity entity = response.getEntity();

			jsonString = convertInputStreamToString(entity.getContent());
			JSONObject jsonObj = new JSONObject(jsonString);

			taskWorker.setConnectorReporter(createErrorReporter(jsonObj, response.getAllHeaders()));

			return jsonObj;
		} catch (Throwable t) {
			log.error(String.format("call (outer catch): %s / %s = %s", l, l2, jsonString), t);
		}
		return null;
	}

	private IConnectorReporter createErrorReporter(JSONObject obj, Header[] headers) throws JSONException {
		ConnectorReporter reporter = new ConnectorReporter();

		if (obj.has("error")) {
			reporter.setDescription(obj.getString("error"));
		}

		if (obj.has("code") && !obj.isNull("code")) {
			reporter.setStatus(obj.getInt("code"));
		}

		if (headers != null) {
			ArrayList<String> list = new ArrayList<String>();

			for (Header header : headers) {
				list.add(String.format("%s = %s", header.getName(), header.getValue()));
			}

			reporter.setHeaders(list);
		}

		return reporter;
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

		if (overrideSsl) {
			registry.register(new Scheme("https", new OverridenSSLSocketFactory(), 443));
		} else {
			final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
			sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			registry.register(new Scheme("https", sslSocketFactory, 443));
		}

		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(ps, registry);

		client = new DefaultHttpClient(manager, ps);
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(12, true));

		if (includeGzip) {
			client.addRequestInterceptor(new GZipRequestInterceptor());
			client.addResponseInterceptor(new GZipResponseInterceptor());
		}

		CookieStore cookieStore = new BasicCookieStore();
		client.setCookieStore(cookieStore);

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

	private static boolean isMultipart(HashMap<String, Object> other) {
		Set<Entry<String, Object>> set = other.entrySet();
		Iterator<Entry<String, Object>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, Object> curr = (Entry<String, Object>) it.next();
			if (curr.getValue() instanceof File) {
				return true;
			}
		}

		return false;
	}
}
