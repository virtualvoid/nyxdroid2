package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.v2.data.PushNotificationResponse;
import sk.virtualvoid.nyxdroid.v2.data.query.PushNotificationQuery;
import android.content.Context;

/**
 * 
 * @author Juraj
 * 
 */
public class PushNotificationDataAccess {
	
	public static Task<PushNotificationQuery, PushNotificationResponse> register(Context context, TaskListener<PushNotificationResponse> listener) {
		return new Task<PushNotificationQuery, PushNotificationResponse>(context, new RegisterTaskWorker(), listener);
	}

	public static Task<PushNotificationQuery, PushNotificationResponse> unregister(Context context, TaskListener<PushNotificationResponse> listener) {
		return new Task<PushNotificationQuery, PushNotificationResponse>(context, new UnregisterTaskWorker(), listener);
	}
	
	public static class RegisterTaskWorker extends TaskWorker<PushNotificationQuery, PushNotificationResponse> {
		@Override
		public PushNotificationResponse doWork(PushNotificationQuery input) throws NyxException {
			PushNotificationResponse result = new PushNotificationResponse();
			result.ActionRequested = PushNotificationResponse.ACTION_REGISTER;
			
			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("regid", input.RegistrationId);

			JSONObject json = connector.call("gcm", "register", params, this);
			if (json == null || !json.has("result") || json.isNull("result")) {
				throw new NyxException("Empty gcm-registrar or authorization error.");
			} else {
				try {
					result.ResultData = json.getString("result");
					result.Success = json.getString("result").equalsIgnoreCase("ok");
				} catch (JSONException e) {
					throw new NyxException(e);
				}
			}
			
			return result;
		}
	}

	public static class UnregisterTaskWorker extends TaskWorker<PushNotificationQuery, PushNotificationResponse> {
		@Override
		public PushNotificationResponse doWork(PushNotificationQuery input) throws NyxException {
			PushNotificationResponse result = new PushNotificationResponse();
			result.ActionRequested = PushNotificationResponse.ACTION_UNREGISTER;

			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("regid", "");

			JSONObject json = connector.call("gcm", "register", params, this);
			if (json == null || !json.has("result") || json.isNull("result")) {
				throw new NyxException("Empty gcm-registrar or authorization error.");
			} else {
				try {
					result.ResultData = json.getString("result");
					result.Success = json.getString("result").equalsIgnoreCase("ok");
				} catch (JSONException e) {
					throw new NyxException(e);
				}
			}
			
			return result;
		}
	}
}
