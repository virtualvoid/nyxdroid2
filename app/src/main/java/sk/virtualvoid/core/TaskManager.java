package sk.virtualvoid.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.os.AsyncTask;

@SuppressWarnings("unchecked")
public class TaskManager {
	private final static Logger log = Logger.getLogger(TaskManager.class);
	private final static ConcurrentHashMap<String, List<Task<?, ?>>> tasks = new ConcurrentHashMap<String, List<Task<?, ?>>>();

	private TaskManager() {
	}

	private static String getTaskKey(Activity owner) {
		String key = null;
		if (owner instanceof ITaskKey) {
			key = ((ITaskKey) owner).getTaskKey();
		} else {
			key = owner.getClass().getCanonicalName();
		}
		return key;
	}
	
	public static <TInput extends ITaskQuery> void startTask(Task<TInput, ?> task, TInput query) {
		task.setTag(query);
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
	}

	public static void addTask(Activity owner, Task<?, ?> task) {
		String key = getTaskKey(owner);
		List<Task<?, ?>> list = tasks.get(key);
		if (list == null) {
			list = new ArrayList<Task<?, ?>>();
			tasks.put(key, list);
		}
		list.add(task);
	}

	public static void removeTask(Task<?, ?> task) {
		for (Entry<String, List<Task<?, ?>>> taskEntry : tasks.entrySet()) {
			String key = taskEntry.getKey();
			List<Task<?, ?>> list = taskEntry.getValue();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == task) {
					list.remove(i);
					break;
				}
			}
			if (list.size() == 0) {
				tasks.remove(key);
				return;
			}
		}
	}

	public static boolean cancelTasks(Activity owner) {
		String key = getTaskKey(owner);
		List<Task<?, ?>> list = tasks.remove(key);
		if (list == null) {
			return false;
		}

		for (Task<?, ?> task : list) {
			task.context = null;

			if (task.taskWorker != null) {
				task.taskWorker.setContext(null);
			}
			if (task.taskListener != null) {
				task.taskListener.setContext(null);
			}

			task.cancel(true);
		}

		log.warn(String.format("cancelTasks: cancelling tasks for owner %s", key));

		return true;
	}

	public static void killIfNeeded(Task<?, ?> task) {
		if (task != null && task.isAlive()) {
			log.debug(String.format("about to cancel: %s", task.getClass().getCanonicalName()));
			task.cancel(true);
		}
	}
}
