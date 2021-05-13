package sk.virtualvoid.core;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;

/**
 * 
 * @author juraj
 * 
 */
public abstract class TaskListener<TOutput> {
	public static final TaskListener<Void> EmptyListener = new TaskListener<Void>() {
		@Override
		public void done(Void output) {
			Log.d(Constants.TAG,"EmptyListener called");
		}
	};

	private Context context;
	private Object tag;

	public TaskListener() {
		int debug = 0;
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

	public void handleError(Throwable t) {
		if (getContext() != null) {
			Toast.makeText(getContext(), getContext().getResources().getString(R.string.general_error_occured), Toast.LENGTH_LONG).show();
		}

		Log.wtf(Constants.TAG,getClass().getCanonicalName() + " reported error: " + t == null ? "null" : t.getMessage());
	}

	public abstract void done(TOutput output);
}
