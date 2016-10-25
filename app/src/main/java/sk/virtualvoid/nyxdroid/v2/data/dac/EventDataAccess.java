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

	private static Event convert(JSONObject object) throws JSONException {
		Event event = new Event();

		event.Id = object.getLong("id_event");
		event.Nick = object.getString("nick_owner");
		event.Title = object.getString("title");
		event.Summary = object.getString("summary");
		event.Time = object.getLong("time_start");
		event.TimeEnd = object.getLong("time_end");
		event.NewComments = object.has("new_comments") && object.getString("new_comments").equals("yes");
		event.Status = object.getString("my_status");
		event.Location = object.has("location") ? object.getString("location") : "";

		event.Description = object.has("description") ? object.getString("description") : "";
	
		return event;
	}

	private static EventComment convertComment(JSONObject object) throws JSONException {
		EventComment comment = new EventComment();
		
		comment.Id = object.getLong("id_comment");
		comment.Nick = object.getString("nick");
		comment.Text = object.getString("content");
		comment.Time = object.getLong("time");
		
		return comment;
	}
	
	public static class GetEventsTaskWorker extends TaskWorker<EventQuery, ArrayList<Event>> {
		@Override
		public ArrayList<Event> doWork(EventQuery input) throws NyxException {
			ArrayList<Event> results = new ArrayList<Event>();

			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("type", input.Type);

			JSONObject json = connector.call("events", "list", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				if (json.has("events") && !json.isNull("events")) {
					try {
						JSONArray events = json.getJSONArray("events");
						for (int i = 0; i < events.length(); i++) {
							JSONObject object = events.getJSONObject(i);
							results.add(convert(object));
						}
					} catch (JSONException e) {
						log.error("GetEventsTaskWorker", e);
						throw new NyxException(e);
					}
				}
			}

			return results;
		}
	}

	public static class GetEventDetailTaskWorker extends TaskWorker<EventQuery, Event> {
		@Override
		public Event doWork(EventQuery input) throws NyxException {
			Event result = null;

			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("id", Long.toString(input.Id));

			JSONObject json = connector.call("events", "show", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (json.has("event")) {
						result = convert(json.getJSONObject("event"));						
						if (json.has("comments")) {
							JSONArray commentsArray = json.getJSONArray("comments");
							for (int i = 0; i < commentsArray.length(); i++) {
								result.Comments.add(convertComment(commentsArray.getJSONObject(i)));							
							}
						}
					}
				} catch (JSONException e) {
					log.error("GetEventDetailTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return result;
		}
	}

	public static class ChangeStatusTaskWorker extends TaskWorker<EventQuery, Void> {
		@Override
		public Void doWork(EventQuery input) throws NyxException {
			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("id", Long.toString(input.Id));
			params.put("my_status", input.ChangeStatus);

			connector.call("events", "change_status", params, this);

			return null;
		}
	}
}
