package sk.virtualvoid.nyxdroid.v2.data;

/**
 * 
 * @author suchan_j
 *
 */
public class UserSearch extends BasePoco {
	public UserActivity Location;
	
	public UserSearch(String nick) {
		this.Nick = nick;
	}
	
	@Override
	public String toString() {
		return Nick;
	}
}
