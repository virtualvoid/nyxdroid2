package sk.virtualvoid.nyxdroid.v2.data.dac;

import sk.virtualvoid.net.IConnector;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;

/**
 * 
 * @author Juraj
 *
 */
public abstract class BaseDataAccess {
	protected static <T extends BasePoco> T isMine(IConnector IConnector, T instance) {
		instance.IsMine = instance.Nick != null && IConnector.getAuthNick().equalsIgnoreCase(instance.Nick);
		return instance;
	}
}
