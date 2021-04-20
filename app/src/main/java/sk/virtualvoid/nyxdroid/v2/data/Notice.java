package sk.virtualvoid.nyxdroid.v2.data;


/**
 * 
 * @author Juraj
 *
 */
public class Notice extends BasePoco {
	public static final String SECTION_TOPICS = "topics";
	public static final String SECTION_EVENTS = "events";
	
	public NoticeType Type;

	public Long DiscussionId;
	public Long WriteupId;
	public Long CommentId;
	
	public String Section;
	
	public String Content;
	public int Thumbs;
	public long Time;

	public boolean IsNew;

	public Notice(NoticeType type) {
		this.Type = type;
	}
	
	public Notice() {
		this.Type = NoticeType.NONE;
	}

	public void setIsNew(long lastVisitTime) {
		IsNew = lastVisitTime < Time;
	}
}
