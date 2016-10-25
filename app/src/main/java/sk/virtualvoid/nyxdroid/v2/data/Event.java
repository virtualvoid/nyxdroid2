package sk.virtualvoid.nyxdroid.v2.data;

import java.util.ArrayList;


/**
 * 
 * @author Juraj
 * 
 */
public class Event extends BasePoco {
	public String Title;
	public String Summary;
	public String Description;
	public long TimeEnd;
	public long Time;
	public String Status;
	public String Location;
	public boolean NewComments;
	
	public ArrayList<EventComment> Comments;
	
	public Event() {
		this.Comments = new ArrayList<EventComment>();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(String.format("Event ID: %d;", Id));
		sb.append(String.format("Title: %s;", Title));
		sb.append(String.format("TimeStart: %d;", Time));
		sb.append(String.format("TimeEnd: %d;", TimeEnd));
		sb.append(String.format("Status: %s;", Status));
		sb.append(String.format("Location: %s;", Location));
		
		return sb.toString();
	}
}
