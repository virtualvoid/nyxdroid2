package sk.virtualvoid.core;

/**
 * 
 * @author juraj
 * 
 */
public class NyxException extends Throwable {
	private static final long serialVersionUID = -6511845887223578844L;

	public NyxException(String message) {
		super(message);
	}

	public NyxException(Throwable throwable) {
		super(throwable);
	}
}
