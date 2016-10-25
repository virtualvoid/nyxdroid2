package sk.virtualvoid.nyxdroid.v2.data;

import java.util.ArrayList;

/**
 * 
 * @author Juraj
 *
 */
public class WriteupResponse extends BaseResponse {
	public long Id;
	public String Name;
	
	public boolean Booked;
	
	public boolean Owner;
	public boolean CanWrite;
	public boolean CanDelete;
	
	public ArrayList<Writeup> Writeups;
	
	public WriteupResponse() {
		Success = true;
	}
}
