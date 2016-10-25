package sk.virtualvoid.nyxdroid.v2.data.dac;

import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;

/**
 * 
 * @author Juraj
 *
 */
public abstract class BaseDataAccess {
	protected static <T extends BasePoco> T isMine(Connector connector, T instance) {
		instance.IsMine = instance.Nick != null && connector.getAuthNick().equalsIgnoreCase(instance.Nick);
		return instance;
	}
}
