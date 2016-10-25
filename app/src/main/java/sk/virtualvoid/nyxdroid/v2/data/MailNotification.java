package sk.virtualvoid.nyxdroid.v2.data;


/**
 * 
 * @author juraj
 * 
 */
public class MailNotification {
	public int Count;
	public String LastFrom;
	
	public MailNotification() {
		Count = 0;
		LastFrom = null;
	}
	
	public boolean valid() {
		return Count > 0 && LastFrom != null;
	}
}
