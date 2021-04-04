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
	
	public static class GetMailTaskWorker extends TaskWorker<MailQuery, ArrayList<Mail>> {
		@Override
		public ArrayList<Mail> doWork(MailQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class SendMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
		@Override
		public NullResponse doWork(MailQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class ReminderMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
		@Override
		public NullResponse doWork(MailQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class DeleteMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
		@Override
		public NullResponse doWork(MailQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}

	public static class GetNotificationsTaskWorker extends TaskWorker<ITaskQuery, MailNotification> {
		@Override
		public MailNotification doWork(ITaskQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
	
	public static class GetConversationTaskWorker extends TaskWorker<ITaskQuery, ArrayList<Conversation>> {
		@Override
		public ArrayList<Conversation> doWork(ITaskQuery input) throws NyxException {
			throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
