package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
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
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class UnregisterTaskWorker extends TaskWorker<PushNotificationQuery, PushNotificationResponse> {
		@Override
		public PushNotificationResponse doWork(PushNotificationQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
