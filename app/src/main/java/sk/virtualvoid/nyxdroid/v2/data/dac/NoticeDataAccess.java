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
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Notice;
import sk.virtualvoid.nyxdroid.v2.data.NoticeType;
import sk.virtualvoid.nyxdroid.v2.data.query.NoticeQuery;

import android.content.Context;

/**
 * @author Juraj
 */
public class NoticeDataAccess {
    private final static Logger log = Logger.getLogger(NoticeDataAccess.class);

    public static Task<NoticeQuery, ArrayList<Notice>> getNotifications(Context context, TaskListener<ArrayList<Notice>> listener) {
        return new Task<NoticeQuery, ArrayList<Notice>>(context, new GetNoticesTaskWorker(), listener);
    }

    public static class GetNoticesTaskWorker extends TaskWorker<NoticeQuery, ArrayList<Notice>> {
        @Override
        public ArrayList<Notice> doWork(NoticeQuery input) throws NyxException {
            ArrayList<Notice> result = new ArrayList<>();

            Connector connector = new Connector(getContext());
            JSONObject json = connector.get("/notifications");
            if (json == null) {
                throw new NyxException("Json result was null ?");
            } else {
                try {
                    JSONArray notices = json.getJSONArray("notifications");
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
                        result.add(itemNotice);

                        JSONObject details = notice.getJSONObject("details");
                        if (details.has("thumbs_up") && !details.isNull("thumbs_up")) {
                            JSONArray thumbUps = details.getJSONArray("thumbs_up");

                            Notice itemThumbUps = new Notice(NoticeType.THUMBS);
                            itemThumbUps.Id = itemNotice.Id;
                            itemThumbUps.DiscussionId = itemNotice.DiscussionId;
                            itemThumbUps.WriteupId = itemNotice.WriteupId;
                            itemThumbUps.Thumbs = thumbUps.length(); // TODO: ked mame ten array s info, pouzit radsej ten ?

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
                                result.add(itemReply);
                            }
                        }
                    }
                } catch (Throwable t) {
                    log.error("GetNoticesTaskWorker", t);
                    throw new NyxException(t);
                }
            }

            return result;
        }
    }
}
