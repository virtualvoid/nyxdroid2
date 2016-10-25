package sk.virtualvoid.nyxdroid.v2.data.query;

import sk.virtualvoid.core.ITaskQuery;

/**
 * 
 * @author suchan_j
 *
 */
public class UserSearchQuery implements ITaskQuery {
	public String Nick;
	
	public UserSearchQuery(String nick) {
		this.Nick = nick;
	}
}
