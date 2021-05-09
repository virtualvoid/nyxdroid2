package sk.virtualvoid.core;

import android.content.Context;

/**
 * 
 * @author juraj
 *
 */
public abstract class TaskWorker<TInput, TOutput> {	
	private Context context;
	private Object tag;

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

	public abstract TOutput doWork(TInput input) throws NyxException;
}
