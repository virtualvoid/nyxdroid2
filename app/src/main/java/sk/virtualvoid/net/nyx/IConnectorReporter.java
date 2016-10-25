package sk.virtualvoid.net.nyx;

import java.util.ArrayList;

/**
 * 
 * @author Juraj
 *
 */
public interface IConnectorReporter {
	boolean isAuthorizationError();
	boolean isDataError();
	int status();
	boolean error();
	String description();
	ArrayList<String> headers();
}
