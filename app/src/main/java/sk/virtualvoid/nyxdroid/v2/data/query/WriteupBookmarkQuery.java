package sk.virtualvoid.nyxdroid.v2.data.query;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.nyxdroid.v2.internal.WriteupBookmarkQueryType;

/**
 * 
 * @author Juraj
 *
 */
public class WriteupBookmarkQuery implements ITaskQuery {
	public long DiscussionId;
	public Long CategoryId;
	public WriteupBookmarkQueryType QueryType;	
}
