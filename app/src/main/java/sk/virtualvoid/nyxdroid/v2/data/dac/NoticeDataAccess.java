package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Notice;
import sk.virtualvoid.nyxdroid.v2.data.NoticeType;
import sk.virtualvoid.nyxdroid.v2.data.query.NoticeQuery;
import android.content.Context;

/**
 * 
 * @author Juraj
 * 
 */
public class NoticeDataAccess {
	private final static Logger log = Logger.getLogger(NoticeDataAccess.class);
	
	public static Task<NoticeQuery, ArrayList<Notice>> getNotifications(Context context, TaskListener<ArrayList<Notice>> listener) {
		return new Task<NoticeQuery, ArrayList<Notice>>(context, new GetNoticesTaskWorker(), listener);
	}

	public static class GetNoticesTaskWorker extends TaskWorker<NoticeQuery, ArrayList<Notice>> {
		@Override
		public ArrayList<Notice> doWork(NoticeQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
