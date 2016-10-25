package sk.virtualvoid.core;

import org.apache.log4j.Logger;

import sk.virtualvoid.net.nyx.IConnectorReporter;
import sk.virtualvoid.net.nyx.IConnectorReporterHandler;
import sk.virtualvoid.nyxdroid.v2.R;
import android.content.Context;
import android.widget.Toast;

/**
 * 
 * @author juraj
 * 
 */
public abstract class TaskListener<TOutput> {
	protected final static Logger log = Logger.getLogger(TaskListener.class);

	public static final TaskListener<Void> EmptyListener = new TaskListener<Void>() {
		@Override
		public void done(Void output) {
			log.debug("EmptyListener called");
		}
	};

	private Context context;
	private Object tag;

	public TaskListener() {
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

		log.fatal(getClass().getCanonicalName() + " reported error: " + t == null ? "null" : t.getMessage());
	}

	public void handleConnectorReporter(IConnectorReporter connectorReporter) {
		if (getContext() != null && getContext() instanceof IConnectorReporterHandler) {
			((IConnectorReporterHandler) getContext()).handleConnectorReporter(connectorReporter);
		}
	}

	public abstract void done(TOutput output);
}
