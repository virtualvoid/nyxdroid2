package sk.virtualvoid.nyxdroid.v2.data;


/**
 * 
 * @author Juraj
 *
 */
public class Bookmark extends BasePoco {
	public long CategoryId;
	public String Name;
	public int Unread;
	public int Replies;
	
	public void markRead() {
		this.Unread = 0;
		this.Replies = 0;
	}
}
