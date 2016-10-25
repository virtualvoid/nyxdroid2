package sk.virtualvoid.nyxdroid.v2.internal;


/**
 * 
 * @author Juraj
 *
 */
public interface IVotingHandler {
	/**
	 * 
	 * @param position pozicia itemu v adapteri / modeli
	 * @param votingType negativny alebo pozitivny hlas
	 */
	void onVote(int position, VotingType votingType);
}
