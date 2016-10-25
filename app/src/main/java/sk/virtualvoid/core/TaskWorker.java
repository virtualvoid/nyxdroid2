package sk.virtualvoid.core;

import sk.virtualvoid.net.nyx.IConnectorReporter;
import android.content.Context;

/**
 * 
 * @author juraj
 *
 */
public abstract class TaskWorker<TInput, TOutput> {	
	private Context context;
	private Object tag;
	private IConnectorReporter connectorReporter;
	
	public TaskWorker() {
	}
	
	protected Context getContext() {
		return this.context;
	}
	
	void setContext(Context context) {
		this.context = context;
	}
	
	synchronized void setTag(Object tag) {
		this.tag = tag;
	}
	
	synchronized protected Object getTag() {
		return tag;
	}
	
	public synchronized void setConnectorReporter(IConnectorReporter connectorReporter) {
		this.connectorReporter = connectorReporter;
	}
	
	public synchronized IConnectorReporter getConnectorReporter() {
		return connectorReporter;
	}
	
	public abstract TOutput doWork(TInput input) throws NyxException;
}
