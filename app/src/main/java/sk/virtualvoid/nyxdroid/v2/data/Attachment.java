package sk.virtualvoid.nyxdroid.v2.data;

/**
 * 
 * @author Juraj
 *
 */
public class Attachment extends BasePoco {
	public String AttachmentSource;
	
	public Attachment(String attachmentSource) {
		this.AttachmentSource = attachmentSource;
	}
}
