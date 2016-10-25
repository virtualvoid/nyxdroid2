package sk.virtualvoid.nyxdroid.v2.data;

/**
 * 
 * @author Juraj
 * 
 */
public class PushNotificationResponse extends BaseResponse {
	public static final String ACTION_REGISTER = "register";
	public static final String ACTION_UNREGISTER = "unregister";

	public String ActionRequested;
	public String ResultData;
}
