package sk.virtualvoid.nyxdroid.v2.data.query;

import java.io.File;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.internal.VotingType;

/**
 * 
 * @author Juraj
 *
 */
public class WriteupQuery implements ITaskQuery {
	public long Id;
	public Constants.WriteupDirection Direction;
	public Long LastId;
	
	public Long TempId;
	public VotingType VotingType;
	public int VotingPosition;
	public boolean VoteToggle;
	
	public String FilterUser;
	public String FilterContents;
	
	public String Contents;
	
	public File AttachmentSource;
	
	public boolean NavigatingOutside;
	public boolean LastSelected;
	
	public boolean IsDeleting;
	public boolean NewState;

	public WriteupQuery() {
		this.LastId = null;
		this.FilterUser = null;
		this.FilterContents = null;
		this.NavigatingOutside = false;
		this.LastSelected = false;
	}
	
	public boolean isFilterUser() {
		return FilterUser != null && FilterUser.length() != 0;
	}
	
	public boolean isFilterContents() {
		return FilterContents != null && FilterContents.length() != 0;
	}
}
