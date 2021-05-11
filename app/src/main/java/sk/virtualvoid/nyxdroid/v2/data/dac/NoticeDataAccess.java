package sk.virtualvoid.nyxdroid.v2.data.dac;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.ConnectorFactory;
import sk.virtualvoid.net.Error;
import sk.virtualvoid.net.IConnector;
import sk.virtualvoid.net.JSONObjectResult;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Context;
import sk.virtualvoid.nyxdroid.v2.data.Notice;
import sk.virtualvoid.nyxdroid.v2.data.NoticeType;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.query.NoticeQuery;

/**
 * @author Juraj
 */
public class NoticeDataAccess {
    private final static Logger log = Logger.getLogger(NoticeDataAccess.class);

    public static Task<NoticeQuery, SuccessResponse<ArrayList<Notice>>> getNotifications(android.content.Context context, TaskListener<SuccessResponse<ArrayList<Notice>>> listener) {
        return new Task<NoticeQuery, SuccessResponse<ArrayList<Notice>>>(context, new GetNoticesTaskWorker(), listener);
    }

    public static class GetNoticesTaskWorker extends TaskWorker<NoticeQuery, SuccessResponse<ArrayList<Notice>>> {
        @Override
        public SuccessResponse<ArrayList<Notice>> doWork(NoticeQuery input) throws NyxException {
            ArrayList<Notice> result = new ArrayList<>();
            Context context = null;

            IConnector connector = ConnectorFactory.getInstance(getContext());
            JSONObjectResult api = connector.get("/notifications");
            if (!api.isSuccess()) {
                Error error = api.getError();
                throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
            } else {
                try {
                    JSONObject root = api.getJson();
                    context = Context.fromJSONObject(root);

                    JSONArray notices = root.getJSONArray("notifications");
                    for (int noticeIndex = 0; noticeIndex < notices.length(); noticeIndex++) {
                        JSONObject notice = notices.getJSONObject(noticeIndex);

                        JSONObject data = notice.getJSONObject("data");

                        Notice itemNotice = new Notice(NoticeType.NOTICE);
                        itemNotice.Id = itemNotice.WriteupId = data.has("id") ? data.getLong("id") : null;
                        itemNotice.DiscussionId = data.has("discussion_id") ? data.getLong("discussion_id") : null;
                        itemNotice.Section = Notice.SECTION_TOPICS;
                        itemNotice.Time = BasePoco.timeFromString(data.getString("inserted_at"));
                        itemNotice.Nick = data.getString("username");
                        itemNotice.Content = data.getString("content");
                        itemNotice.setIsNew(context.getUser().getNotificationsLastVisit());

                        result.add(itemNotice);

                        JSONObject details = notice.getJSONObject("details");
                        if (details.has("thumbs_up") && !details.isNull("thumbs_up")) {
                            JSONArray thumbUps = details.getJSONArray("thumbs_up");

                            Notice itemThumbUps = new Notice(NoticeType.THUMBS);
                            itemThumbUps.Id = itemNotice.Id;
                            itemThumbUps.DiscussionId = itemNotice.DiscussionId;
                            itemThumbUps.WriteupId = itemNotice.WriteupId;
                            itemThumbUps.Thumbs = thumbUps.length(); // TODO: ked mame ten array s info, pouzit radsej ten ?
                            itemThumbUps.setIsNew(context.getUser().getNotificationsLastVisit());

                            result.add(itemThumbUps);
                        }

                        if (details.has("replies") && !details.isNull("replies")) {
                            JSONArray replies = details.getJSONArray("replies");

                            for (int replyIndex = 0; replyIndex < replies.length(); replyIndex++) {
                                JSONObject reply = replies.getJSONObject(replyIndex);

                                Notice itemReply = new Notice(NoticeType.REPLY);
                                itemReply.Id = itemReply.WriteupId = reply.has("id") ? reply.getLong("id") : null;
                                itemReply.DiscussionId = reply.has("discussion_id") ? reply.getLong("discussion_id") : null;
                                itemReply.Section = Notice.SECTION_TOPICS;
                                itemReply.Time = BasePoco.timeFromString(reply.getString("inserted_at"));
                                itemReply.Nick = reply.getString("username");
                                itemReply.Content = reply.getString("content");
                                itemReply.setIsNew(context.getUser().getNotificationsLastVisit());

                                result.add(itemReply);
                            }
                        }
                    }
                } catch (Throwable t) {
                    log.error("GetNoticesTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return new SuccessResponse<>(result, context);
        }
    }
}
