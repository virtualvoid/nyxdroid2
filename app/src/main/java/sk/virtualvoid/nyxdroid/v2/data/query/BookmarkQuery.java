package sk.virtualvoid.nyxdroid.v2.data.query;

import sk.virtualvoid.core.ITaskQuery;

/**
 * 
 * @author Juraj
 *
 */
public class BookmarkQuery implements ITaskQuery {
	public boolean IncludeUnread;
	public String SearchTerm;
	public Long CategoryId;
}
