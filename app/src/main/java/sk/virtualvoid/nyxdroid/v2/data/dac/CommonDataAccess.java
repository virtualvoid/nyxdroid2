package sk.virtualvoid.nyxdroid.v2.data.dac;

import android.app.Activity;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;

public class CommonDataAccess {
    public static Task<ITaskQuery, NullResponse> clearDrawableCache(Activity context, TaskListener<NullResponse> listener) {
        return new Task<ITaskQuery, NullResponse>(context, new ClearDrawableCacheTaskWorker(), listener);
    }

    public static class ClearDrawableCacheTaskWorker extends TaskWorker<ITaskQuery, NullResponse> {
        @Override
        public NullResponse doWork(ITaskQuery input) throws NyxException {
            return null;
        }
    }
}
