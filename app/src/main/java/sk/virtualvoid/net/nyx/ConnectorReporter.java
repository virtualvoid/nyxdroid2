package sk.virtualvoid.net.nyx;

import java.util.ArrayList;

/**
 * 
 * @author Juraj
 *
 */
public class ConnectorReporter implements IConnectorReporter {
	public static final int OK = 0;
	public static final int[] DATA_ERROR = new int[] { 400, 404 };
	public static final int AUTHORIZATION_ERROR = 401;
	
	private int status;
	private String description;
	private ArrayList<String> headers;
	
	public ConnectorReporter() {
		status = OK;
		description = null;
		headers = new ArrayList<String>();
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}
	
	@Override
	public boolean isAuthorizationError() {
		return status != OK && status == AUTHORIZATION_ERROR;
	}

	@Override
	public boolean isDataError() {
		boolean dataError = false;
		
		for (int code : DATA_ERROR) {
			if (code == status) {
				dataError = true;
				break;
			}
		}
		
		return status != OK && dataError;
	}

	@Override
	public boolean error() {
		return status != OK;
	}

	@Override
	public int status() {
		return status;
	}
	
	@Override
	public String description() {
		return description;
	}

	@Override
	public ArrayList<String> headers() {
		return headers;
	}
}
