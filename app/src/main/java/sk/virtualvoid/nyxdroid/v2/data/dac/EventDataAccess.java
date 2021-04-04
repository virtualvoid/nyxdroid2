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
import sk.virtualvoid.nyxdroid.v2.data.Event;
import sk.virtualvoid.nyxdroid.v2.data.EventComment;
import sk.virtualvoid.nyxdroid.v2.data.query.EventQuery;
import android.app.Activity;

/**
 * 
 * @author Juraj
 * 
 */
public class EventDataAccess {
	private final static Logger log = Logger.getLogger(EventDataAccess.class);

	public static Task<EventQuery, ArrayList<Event>> getEvents(Activity context, TaskListener<ArrayList<Event>> listener) {
		return new Task<EventQuery, ArrayList<Event>>(context, new GetEventsTaskWorker(), listener);
	}

	public static Task<EventQuery, Event> getEventDetail(Activity context, TaskListener<Event> listener) {
		return new Task<EventQuery, Event>(context, new GetEventDetailTaskWorker(), listener);
	}

	public static Task<EventQuery, Void> changeStatus(Activity context, TaskListener<Void> listener) {
		return new Task<EventQuery, Void>(context, new ChangeStatusTaskWorker(), listener);
	}


	public static class GetEventsTaskWorker extends TaskWorker<EventQuery, ArrayList<Event>> {
		@Override
		public ArrayList<Event> doWork(EventQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class GetEventDetailTaskWorker extends TaskWorker<EventQuery, Event> {
		@Override
		public Event doWork(EventQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class ChangeStatusTaskWorker extends TaskWorker<EventQuery, Void> {
		@Override
		public Void doWork(EventQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
