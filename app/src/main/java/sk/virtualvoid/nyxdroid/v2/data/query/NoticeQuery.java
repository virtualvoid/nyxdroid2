package sk.virtualvoid.nyxdroid.v2.data.query;

import sk.virtualvoid.core.ITaskQuery;

/**
 * 
 * @author Juraj
 *
 */
public class NoticeQuery implements ITaskQuery {
	public boolean KeepNew;
	
	public NoticeQuery() {
		KeepNew = false;
	}
}
