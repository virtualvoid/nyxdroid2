package sk.virtualvoid.nyxdroid.v2.data.query;

import java.util.ArrayList;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.nyxdroid.v2.data.Attachment;

/**
 * 
 * @author juraj
 * 
 */
public class MailQuery implements ITaskQuery {
	public boolean Refresh;
	public Long FirstId;
	public Long LastId;
	public String FilterUser;
	public String FilterText;

	public Long Id;
	public String To;
	public String Message;
	public ArrayList<Attachment> Attachments = new ArrayList<>();

	public boolean NewState;

	public boolean isFilterUser() {
		return FilterUser != null && FilterUser.length() > 0;
	}

	public boolean isFilterText() {
		return FilterText != null && FilterText.length() > 0;
	}
}
