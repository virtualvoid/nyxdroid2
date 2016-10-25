package sk.virtualvoid.nyxdroid.v2.internal;

import java.util.ArrayList;

/**
 * 
 * @author Juraj
 *
 */
public class VotingInfoResult {
	public int My;
	public int Total;
	
	public int Positive;
	public ArrayList<String> PositiveList;
	public boolean MeVotedPositive;
	
	public int Negative;
	public ArrayList<String> NegativeList;
	public boolean MeVotedNegative;
	
	public VotingInfoResult() {
		PositiveList = new ArrayList<String>();
		MeVotedPositive = false;
		
		NegativeList = new ArrayList<String>();
		MeVotedNegative = false;
	}
}
