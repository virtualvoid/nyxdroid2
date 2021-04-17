package sk.virtualvoid.nyxdroid.v2.data.query;

import sk.virtualvoid.core.ITaskQuery;

/**
 * 
 * @author Juraj
 *
 */
public class PushNotificationQuery implements ITaskQuery {	
	public String RegistrationId;

	public PushNotificationQuery(String registrationId) {
		RegistrationId = registrationId;
	}
}
