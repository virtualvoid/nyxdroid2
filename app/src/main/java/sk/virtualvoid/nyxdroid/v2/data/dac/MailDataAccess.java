package sk.virtualvoid.nyxdroid.v2.data.dac;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.ConnectorFactory;
import sk.virtualvoid.net.Error;
import sk.virtualvoid.net.IConnector;
import sk.virtualvoid.net.JSONObjectResult;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Conversation;
import sk.virtualvoid.nyxdroid.v2.data.Mail;
import sk.virtualvoid.nyxdroid.v2.data.MailNotification;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.WaitingFile;
import sk.virtualvoid.nyxdroid.v2.data.query.MailQuery;

/**
 * @author juraj
 */
public class MailDataAccess {
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
            ArrayList<Mail> result = new ArrayList<Mail>();

            IConnector connector = ConnectorFactory.getInstance(getContext());

            String baseUrl = "/mail";

            if (input.FirstId == null && input.LastId != null) {
                baseUrl = baseUrl + "?order=older_than&from_id=" + input.LastId;
            }

            if (input.FirstId != null && input.LastId == null) {
                baseUrl = baseUrl + "?order=newer_than&from_id=" + input.FirstId;
            }

            if (input.isFilterUser()) {
                if (!baseUrl.contains("?")) {
                    baseUrl = baseUrl + "?";
                } else {
                    baseUrl = baseUrl + "&";
                }
                baseUrl = baseUrl + "user=" + input.FilterUser;
            }
            if (input.isFilterText()) {
                if (!baseUrl.contains("?")) {
                    baseUrl = baseUrl + "?";
                } else {
                    baseUrl = baseUrl + "&";
                }
                baseUrl = baseUrl + "text=" + input.FilterText;
            }

            JSONObjectResult api = connector.get(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();
                    if (root.has("posts") && !root.isNull("posts")) {
                        JSONArray posts = root.getJSONArray("posts");

                        for (int mailIndex = 0; mailIndex < posts.length(); mailIndex++) {
                            JSONObject post = posts.getJSONObject(mailIndex);

                            Mail mail = new Mail();

                            mail.Id = post.getLong("id");
                            mail.Nick = post.getString("username");
                            mail.Content = post.getString("content");

                            if (post.has("incoming") && post.getBoolean("incoming")) {
                                mail.Direction = Constants.FROM;
                            } else {
                                mail.Direction = Constants.TO;
                            }

                            mail.IsUnread = post.has("unread") && post.getBoolean("unread");
                            mail.IsReminded = post.has("reminder") && post.getBoolean("reminder");

                            mail.IsMine = connector.getAuthNick().equalsIgnoreCase(post.getString("username"));

                            mail.Time = BasePoco.timeFromString(post.getString("inserted_at"));

                            result.add(mail);
                        }
                    }
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "GetMailTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }

    public static class SendMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
        @Override
        public NullResponse doWork(MailQuery input) throws NyxException {
            IConnector connector = ConnectorFactory.getInstance(getContext());

            JSONObjectResult api = null;
            WaitingFile waitingFile = null;

            if (input.AttachmentSource != null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("file", input.AttachmentSource);
                map.put("file_type", "mail_attachment");
                map.put("id_specific", 0L);

                api = connector.multipart("/file/upload", map);
                if (!api.isSuccess()) {
                    Error error = api.getError();
                    throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
                }
                waitingFile = WaitingFile.fromJSONObject(api.getJson());
            }

            HashMap<String, String> form = new HashMap<>();
            form.put("recipient", input.To);
            form.put("message", input.Message);

            api = connector.form("/mail/send", form);
            // TODO: check if the call was successful
            return NullResponse.success();
        }
    }

    public static class ReminderMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
        @Override
        public NullResponse doWork(MailQuery input) throws NyxException {
            IConnector connector = ConnectorFactory.getInstance(getContext());

            String baseUrl = "/mail/reminder/" + input.Id + "/" + input.NewState;
            JSONObjectResult api = connector.post(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            }
            return NullResponse.success();
        }
    }

    public static class DeleteMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
        @Override
        public NullResponse doWork(MailQuery input) throws NyxException {
            IConnector connector = ConnectorFactory.getInstance(getContext());
            JSONObjectResult api = connector.delete("/mail/delete/" + input.Id);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            }
            return NullResponse.success();
        }
    }

    public static class GetNotificationsTaskWorker extends TaskWorker<ITaskQuery, MailNotification> {
        @Override
        public MailNotification doWork(ITaskQuery input) throws NyxException {
            MailNotification result = new MailNotification();

            IConnector connector = ConnectorFactory.getInstance(getContext());

            String baseUrl = "/status";
            JSONObjectResult api = connector.get(baseUrl);
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();
                    if (root.has("user") && !root.isNull("user")) {
                        JSONObject user = root.getJSONObject("user");

                        result.LastFrom = user.getString("mail_last_from");
                        result.Count = user.getInt("mail_unread");
                    }
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "GetNotificationsTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }

    public static class GetConversationTaskWorker extends TaskWorker<ITaskQuery, ArrayList<Conversation>> {
        @Override
        public ArrayList<Conversation> doWork(ITaskQuery input) throws NyxException {
            ArrayList<Conversation> result = new ArrayList<>();

            IConnector connector = ConnectorFactory.getInstance(getContext());

            JSONObjectResult api = connector.get("/mail");
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();
                    if (root.has("conversations") && !root.isNull("conversations")) {
                        JSONArray conversations = root.getJSONArray("conversations");
                        for (int conversationIndex = 0; conversationIndex < conversations.length(); conversationIndex++) {
                            JSONObject conversation = conversations.getJSONObject(conversationIndex);

                            result.add(
                                    new Conversation(
                                            BasePoco.timeFromString(conversation.getString("conversed_at")),
                                            (conversation.has("incoming") && conversation.getBoolean("incoming")) ? Constants.FROM : Constants.TO,
                                            conversation.getString("username")
                                    )
                            );
                        }
                    }
                } catch (Throwable t) {
                    Log.e(Constants.TAG, "GetConversationTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }
}
