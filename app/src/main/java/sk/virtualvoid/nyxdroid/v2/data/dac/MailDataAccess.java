package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Conversation;
import sk.virtualvoid.nyxdroid.v2.data.Mail;
import sk.virtualvoid.nyxdroid.v2.data.MailNotification;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.UserActivity;
import sk.virtualvoid.nyxdroid.v2.data.query.MailQuery;

import android.app.Activity;
import android.content.Context;

/**
 * @author juraj
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
            ArrayList<Mail> result = new ArrayList<Mail>();

            Connector connector = new Connector(getContext());

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

            JSONObject root = connector.get(baseUrl);
            if (root == null) {
                throw new NyxException("Json result was null ?");
            } else {
                try {
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

                            mail.IsMine = connector.getAuthNick().equalsIgnoreCase(post.getString("username"));

                            mail.Time = BasePoco.timeFromString(post.getString("inserted_at"));

                            result.add(mail);
                        }
                    }
                } catch (Throwable t) {
                    log.error("GetMailTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }

    public static class SendMailTaskWorker extends TaskWorker<MailQuery, NullResponse> {
        @Override
        public NullResponse doWork(MailQuery input) throws NyxException {
            Connector connector = new Connector(getContext());

            List<NameValuePair> form = new ArrayList<NameValuePair>();
            form.add(new BasicNameValuePair("recipient", input.To));
            form.add(new BasicNameValuePair("message", input.Message));
            //form.add(new BasicNameValuePair("format", "text/plain"));

            JSONObject json = connector.form("/mail/send", form);

            return NullResponse.success();
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
            Connector connector = new Connector(getContext());
            JSONObject json = connector.delete("/mail/delete/" + input.Id);
            return NullResponse.success();
        }
    }

    public static class GetNotificationsTaskWorker extends TaskWorker<ITaskQuery, MailNotification> {
        @Override
        public MailNotification doWork(ITaskQuery input) throws NyxException {
            MailNotification result = new MailNotification();

            Connector connector = new Connector(getContext());

            String baseUrl = "/status";
            JSONObject root = connector.get(baseUrl);
            if (root == null) {
                throw new NyxException("Json result was null ?");
            } else {
                try {
                    if (root.has("user") && !root.isNull("user")) {
                        JSONObject user = root.getJSONObject("user");

                        result.LastFrom = user.getString("mail_last_from");
                        result.Count = user.getInt("mail_unread");
                    }
                } catch (Throwable t) {
                    log.error("GetNotificationsTaskWorker", t);
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

            Connector connector = new Connector(getContext());

            JSONObject root = connector.get("/mail");
            if (root == null) {
                throw new NyxException("Json result was null ?");
            } else {
                try {
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
                    log.error("GetConversationTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }
}
