package sk.virtualvoid.nyxdroid.v2.data.dac;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import android.app.Activity;

/**
 * 
 * @author Juraj
 *
 */
public class UserActivityDataAccess {
	private final static Logger log = Logger.getLogger(UserActivityDataAccess.class);
	
	public static Task<ITaskQuery, NullResponse> inactivate(Activity context, TaskListener<NullResponse> listener) {
		return new Task<ITaskQuery, NullResponse>(context, new InactivateTaskWorker(), listener);
	}
	
	public static Task<ITaskQuery, NullResponse> clearCredentials(Activity context, TaskListener<NullResponse> listener) {
		return new Task<ITaskQuery, NullResponse>(context, new ClearCredentialsTaskWorker(), listener);
	}
	
	public static class InactivateTaskWorker extends TaskWorker<ITaskQuery, NullResponse> {
		@Override
		public NullResponse doWork(ITaskQuery input) throws NyxException {
			NullResponse result = new NullResponse();
			try {
				Connector connector = new Connector(getContext());
				JSONObject obj = connector.call("util", "make_inactive", Connector.EmptyParams, this);
				String strResult = obj.getString("result");
				result.Success = strResult != null && strResult.equalsIgnoreCase(Constants.OK);
			} catch (Throwable e) {
				log.error("InactivateTaskWorker", e);
				throw new NyxException(e);
			}
			return result;
		}
	}
	
	public static class ClearCredentialsTaskWorker extends TaskWorker<ITaskQuery, NullResponse> {
		@Override
		public NullResponse doWork(ITaskQuery input) throws NyxException {
			NullResponse result = new NullResponse();
			try {
				Connector connector = new Connector(getContext());
				JSONObject obj = connector.call("util", "remove_authorization", Connector.EmptyParams, this);
				String strResult = obj.getString("result");
				result.Success = strResult != null && strResult.equalsIgnoreCase(Constants.OK);
			} catch (Throwable e) {
				log.error("ClearCredentialsTaskWorker", e);
				throw new NyxException(e);
			}
			return result;
		}
	}
}
