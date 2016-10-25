package sk.virtualvoid.nyxdroid.v2.internal;

/**
 * 
 * @author Juraj
 *
 */
public interface INavigationHandler {
	boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId);
}
