package sk.virtualvoid.core;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import sk.virtualvoid.nyxdroid.library.Constants;

/**
 * @param <TInput>
 * @param <TOutput>
 * @author juraj
 */
public class Task<TInput extends ITaskQuery, TOutput> extends AsyncTask<TInput, Void, TOutput> {
    Context context;
    TaskListener<TOutput> taskListener;
    TaskWorker<TInput, TOutput> taskWorker;
    Throwable lastError;

    public Task(Context context, TaskWorker<TInput, TOutput> taskWorker, TaskListener<TOutput> taskListener) {
        this.context = context;

        this.taskListener = taskListener;
        this.taskListener.setContext(context);

        this.taskWorker = taskWorker;
        this.taskWorker.setContext(context);

        this.lastError = null;
    }

    public synchronized void setTag(Object tag) {
        this.taskListener.setTag(tag);
        this.taskWorker.setTag(tag);
    }

    //private static void execute(ITaskQuery... query) { }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (context != null && context instanceof Activity) {
            Activity activity = ((Activity) context);

            activity.setProgressBarIndeterminateVisibility(true);
            TaskManager.addTask(activity, this);
        }

        if (context == null) {
            Log.wtf(Constants.TAG, String.format("onPreExecute: %s doesn't have context attached !", getClass().getCanonicalName()));
        }
    }

    @Override
    protected TOutput doInBackground(TInput... params) {
        try {
            return taskWorker.doWork(params == null || params.length == 0 ? null : params[0]);
        } catch (Throwable t) {
            Log.e(Constants.TAG, "doInBackground", t);
            lastError = t;
        }
        return null;
    }

    private void handlePostExecute(TOutput result) {
        if (context != null && context instanceof Activity) {
            Activity activity = ((Activity) context);
            activity.setProgressBarIndeterminateVisibility(false);
        }

        TaskManager.removeTask(this);

        if (isCancelled()) {
            return;
        }

        if (context != null) {
            if (taskListener != null && taskWorker != null) {
                if (lastError != null) {
                    taskListener.handleError(lastError);
                } else {
                    taskListener.done(result);
                }
            } else {
                Log.wtf(Constants.TAG, String.format("onPostExecute: %s doesn't have LISTENER/WORKER, WTF?", getClass().getCanonicalName()));
            }
        } else {
            Log.wtf(Constants.TAG, String.format("onPostExecute: %s doesn't have context attached !", getClass().getCanonicalName()));
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        handlePostExecute(null);

        Log.d(Constants.TAG,String.format("onCancelled: %s execution cancelled: no reason.", getClass().getCanonicalName()));
    }

    @Override
    protected void onPostExecute(TOutput result) {
        super.onPostExecute(result);

        handlePostExecute(result);
    }

    public boolean isAlive() {
        return getStatus() == Status.RUNNING || getStatus() == Status.PENDING;
    }
}
