package sk.virtualvoid.nyxdroid.v2.data;

/**
 * 
 * @author suchan_j
 *
 */
public class Conversation extends BasePoco {
	public long Time;
	public String Direction;

	public Conversation(long time, String direction, String nick) {
		Time = time;
		Direction = direction;
		Nick = nick;
	}
}
