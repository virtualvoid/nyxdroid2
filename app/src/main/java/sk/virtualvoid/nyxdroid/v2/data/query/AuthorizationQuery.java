package sk.virtualvoid.nyxdroid.v2.data.query;

import sk.virtualvoid.core.ITaskQuery;

/**
 * 
 * @author Juraj
 * 
 */
public class AuthorizationQuery implements ITaskQuery {
	public String Nick;
	
	public AuthorizationQuery(String nick) {
		this.Nick = nick;
	}
}
