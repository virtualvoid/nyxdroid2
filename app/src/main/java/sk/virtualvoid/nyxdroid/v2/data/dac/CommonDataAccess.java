package sk.virtualvoid.nyxdroid.v2.data.dac;

import org.apache.log4j.Logger;

import android.app.Activity;

import com.nostra13.universalimageloader.core.ImageLoader;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;

public class CommonDataAccess {
	private final static Logger log = Logger.getLogger(CommonDataAccess.class);

	public static Task<ITaskQuery, NullResponse> clearDrawableCache(Activity context, TaskListener<NullResponse> listener) {
		return new Task<ITaskQuery, NullResponse>(context, new ClearDrawableCacheTaskWorker(), listener);
	}
	
	public static class ClearDrawableCacheTaskWorker extends TaskWorker<ITaskQuery, NullResponse> {
		@Override
		public NullResponse doWork(ITaskQuery input) throws NyxException {
			log.debug("ClearDrawableCacheTaskWorker preparing");
			
			ImageLoader imageLoader = ImageLoader.getInstance();
			imageLoader.clearMemoryCache();
			imageLoader.clearDiscCache();
			
			log.debug("ClearDrawableCacheTaskWorker executed.");
			
			return null;
		}
	}
}
