package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Conversation;
import sk.virtualvoid.nyxdroid.v2.data.Mail;
import sk.virtualvoid.nyxdroid.v2.data.MailNotification;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.UserActivity;
import sk.virtualvoid.nyxdroid.v2.data.query.MailQuery;
import android.app.Activity;
import android.content.Context;

/**
 * 
 * @author juraj
 * 
 */
public class MailDataAccess {
	private final static Logger log = Logger.getLogger(MailDataAccess.class);
	
	public static Task<MailQuery, ArrayList<Mail>> getMail(Activity context, TaskListener<ArrayList<Mail>> listener) {
		return new Task<MailQuery, ArrayList<Mail>>(context, new GetMailTaskWorker(), listener);
	}

	public static Task<MailQuery, NullResponse> sendMail(Activity context, TaskListener<NullResponse> listener) {
		return new Task<MailQuery, NullResponse>(context, new SendMailTaskWorker(), listener);
	}

	public static Task<MailQuery, NullResponse> deleteMail(Activity context, TaskListener<NullResponse> listener) {
		return new Task<MailQuery, NullResponse>(context, new DeleteMailTaskWorker(), listener);
	}

	public static Task<MailQuery, NullResponse> reminderMail(Activity context, TaskListener<NullResponse> listener) {
		return new Task<MailQuery, NullResponse>(context, new ReminderMailTaskWorker(), listener);
	}

	public static Task<ITaskQuery, MailNotification> getNotifications(Context context, TaskListener<MailNotification> listener) {
		return new Task<ITaskQuery, MailNotification>(context, new GetNotificationsTaskWorker(), listener);
	}

	public static Task<ITaskQuery, ArrayList<Conversation>> getConversations(Context context, TaskListener<ArrayList<Conversation>> listener) {
		return new Task<ITaskQuery, ArrayList<Conversation>>(context, new GetConversationTaskWorker(), listener);
	}
	
	private static Mail convert(JSONObject obj) throws JSONException {
		Mail mail = new Mail();
		mail.Id = obj.getLong("id_mail");
		mail.Nick = obj.getString("other_nick");
		mail.Direction = obj.getString("direction");
		mail.Time = obj.getLong("time");
		mail.Content = obj.getString("content");

		boolean unread = false, otherSawIt = true;
		if (obj.has("new")) {
			unread = obj.getString("new").equals("yes");
		}
		if (obj.has("message_status") && obj.getString("message_status").equals("unread")) {
			otherSawIt = false;
		}
		mail.IsUnread = unread;
		mail.OtherSawIt = otherSawIt;

		mail.Location = UserActivity.fromJson(obj);

		return mail;
	}
	
	private static Conversation convertConversation(JSONObject obj) throws JSONException {
		Conversation conversation = new Conversation();
		
		conversation.Nick = obj.getString("nick");
		conversation.Time = obj.getLong("time");
		conversation.Direction = obj.getString("direction");
		
		return conversation;
	}

	public static class GetMailTaskWorker extends TaskWorker<MailQuery, ArrayList<Mail>> {
		@Override
		public ArrayList<Mail> doWork(MailQuery input) throws NyxException {
			ArrayList<Mail> mailList = new ArrayList<Mail>();

			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			if (!input.isFilterUser() && !input.isFilterText()) {
				if (input.FirstId == null && input.LastId == null) {
					params.put("direction", "newest");
				}
				if (input.FirstId == null && input.LastId != null) {
					params.put("direction", "older");
					params.put("id_mail", Long.toString(input.LastId));
				}
				if (input.FirstId != null && input.LastId == null) {
					params.put("direction", "newer");
					params.put("id_mail", Long.toString(input.FirstId));
				}
			} else {
				params.put("direction", "newest");
				params.put("id_mail", "0");

				if (input.isFilterUser()) {
					params.put("filter_user", input.FilterUser);
				}
				if (input.isFilterText()) {
					params.put("filter_text", input.FilterText);
				}
			}

			JSONObject json = connector.call("mail", "messages", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						JSONArray array = json.getJSONArray("data");
						for (int i = 0; i < array.length(); i++) {
							JSONObject obj = array.getJSONObject(i);
							mailList.add(convert(obj));
						}
					}
				} catch (JSONException e) {
					log.error("GetMailTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return mailList;
		}
	}

	public static class SendMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
		@Override
		public NullResponse doWork(MailQuery input) throws NyxException {
			NullResponse result = new NullResponse();
			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("recipient", input.To);
			params.put("message", input.Message);
			
			if (input.AttachmentSource != null) {
				params.put("attachment", input.AttachmentSource);
			}

			try {
				JSONObject json = connector.call("mail", "send", params, this);
				if (json == null || !json.has("result") || json.isNull("result")) {
					throw new NyxException("Json result was null ?");
				} else {
					String strResult = json.getString("result");
					result.Success = strResult != null && strResult.equalsIgnoreCase(Constants.OK);
				}
			} catch (JSONException e) {
				log.error("SendMailTaskWorker", e);
				throw new NyxException(e);
			}
			return result;
		}
	}

	public static class ReminderMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
		@Override
		public NullResponse doWork(MailQuery input) throws NyxException {
			NullResponse result = new NullResponse();
			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("id_mail", Long.toString(input.Id));
			params.put("reminder", "1");

			try {
				JSONObject json = connector.call("mail", "reminder", params, this);
				if (json == null || !json.has("result") || json.isNull("result")) {
					throw new NyxException("Json result was null ?");
				} else {
					String strResult = json.getString("result");
					result.Success = strResult != null && strResult.equalsIgnoreCase(Constants.OK);
				}
			} catch (JSONException e) {
				log.error("ReminderMailTaskWorker", e);
				throw new NyxException(e);
			}
			return result;
		}
	}

	public static class DeleteMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
		@Override
		public NullResponse doWork(MailQuery input) throws NyxException {
			NullResponse result = new NullResponse();
			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("id_mail", Long.toString(input.Id));

			try {
				JSONObject json = connector.call("mail", "delete", params, this);
				if (json == null || !json.has("result") || json.isNull("result")) {
					throw new NyxException("Json result was null ?");
				} else {
					String strResult = json.getString("result");
					result.Success = strResult != null && strResult.equalsIgnoreCase(Constants.OK);
				}
			} catch (JSONException e) {
				log.error("DeleteMailTaskWorker", e);
				throw new NyxException(e);
			}
			return result;
		}
	}

	public static class GetNotificationsTaskWorker extends TaskWorker<ITaskQuery, MailNotification> {
		@Override
		public MailNotification doWork(ITaskQuery input) throws NyxException {
			MailNotification result = new MailNotification();

			Connector connector = new Connector(getContext());

			JSONObject json = connector.call("help", "test", Connector.EmptyParams, this);
			if (json != null && json.has("system")) {
				try {
					JSONObject obj = json.getJSONObject("system");
					if (obj.has("unread_post") && obj.has("unread_post_last_from")) {
						result.Count = obj.getInt("unread_post");
						result.LastFrom = obj.getString("unread_post_last_from");
					}
				} catch (JSONException e) {
					log.error("GetNotificationsTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return result;
		}
	}
	
	public static class GetConversationTaskWorker extends TaskWorker<ITaskQuery, ArrayList<Conversation>> {
		@Override
		public ArrayList<Conversation> doWork(ITaskQuery input) throws NyxException {
			ArrayList<Conversation> result = new ArrayList<Conversation>();
			
			Connector connector = new Connector(getContext());
			JSONObject json = connector.call("mail", "conversations", Connector.EmptyParams, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("conversations")) {
						JSONArray array = json.getJSONArray("conversations");
						for (int i = 0; i < array.length(); i++) {
							JSONObject obj = array.getJSONObject(i);
							result.add(convertConversation(obj));							
						}
					}
				} catch (JSONException e) {
					log.error("GetConversationTaskWorker", e);
					throw new NyxException(e);
				}
			}
			
			return result;
		}
	}
}
