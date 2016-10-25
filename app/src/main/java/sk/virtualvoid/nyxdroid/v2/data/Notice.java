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
	public boolean IsNew;
	public int Thumbs;
	public long Time;
	
	public Notice(NoticeType type) {
		this.Type = type;
	}
	
	public Notice() {
		this.Type = NoticeType.NONE;
	}
}
