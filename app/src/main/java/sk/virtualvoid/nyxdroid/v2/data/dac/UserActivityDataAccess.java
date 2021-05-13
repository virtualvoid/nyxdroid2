package sk.virtualvoid.nyxdroid.v2.data.dac;

import android.app.Activity;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;

/**
 * 
 * @author Juraj
 *
 */
public class UserActivityDataAccess {

	public static Task<ITaskQuery, NullResponse> inactivate(Activity context, TaskListener<NullResponse> listener) {
		return new Task<ITaskQuery, NullResponse>(context, new InactivateTaskWorker(), listener);
	}
	
	public static Task<ITaskQuery, NullResponse> clearCredentials(Activity context, TaskListener<NullResponse> listener) {
		return new Task<ITaskQuery, NullResponse>(context, new ClearCredentialsTaskWorker(), listener);
	}
	
	public static class InactivateTaskWorker extends TaskWorker<ITaskQuery, NullResponse> {
		@Override
		public NullResponse doWork(ITaskQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
	
	public static class ClearCredentialsTaskWorker extends TaskWorker<ITaskQuery, NullResponse> {
		@Override
		public NullResponse doWork(ITaskQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
