package sk.virtualvoid.nyxdroid.v2.internal;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.nyxdroid.v2.data.PushNotificationResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.PushNotificationDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.PushNotificationQuery;
import android.content.Context;

import com.google.android.gcm.GCMRegistrar;

/**
 * 
 * @author Juraj
 * 
 */
public class PushNotificationRegistrar {
	private static final PushNotificationTaskListener taskListener = new PushNotificationTaskListener();

	public static void register(Context context, String registrationId) {
		PushNotificationQuery query = new PushNotificationQuery();
		query.RegistrationId = registrationId;
		
		Task<PushNotificationQuery, PushNotificationResponse> task = PushNotificationDataAccess.register(context, taskListener);
		task.execute(query);
	}

	public static void unregister(Context context) {
		PushNotificationQuery query = new PushNotificationQuery();
		Task<PushNotificationQuery, PushNotificationResponse> task = PushNotificationDataAccess.register(context, taskListener);
		task.execute(query);
	}
	
	/**
	 * 
	 * @author Juraj
	 * 
	 */
	private static class PushNotificationTaskListener extends TaskListener<PushNotificationResponse> {
		@Override
		public void done(PushNotificationResponse output) {
			if (output.ActionRequested.equals(PushNotificationResponse.ACTION_REGISTER)) {
				handleRegistrationCompleted(output);
			}

			if (output.ActionRequested.equals(PushNotificationResponse.ACTION_UNREGISTER)) {
				handleUnregistrationCompleted(output);
			}
		}

		private void handleRegistrationCompleted(PushNotificationResponse output) {
			GCMRegistrar.setRegisteredOnServer(getContext(), true);
		}

		private void handleUnregistrationCompleted(PushNotificationResponse output) {
			GCMRegistrar.setRegisteredOnServer(getContext(), false);
		}
	}
}
