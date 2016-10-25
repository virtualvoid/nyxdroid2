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
			ArrayList<Notice> results = new ArrayList<Notice>();

			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("keep_new", input.KeepNew ? "1" : "0");

			JSONObject json = connector.call("feed", "notices", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						JSONObject data = json.getJSONObject("data");

						long lastVisit = data.getLong("notice_last_visit");

						if (data.has("items") && !data.isNull("items")) {
							JSONArray items = data.getJSONArray("items");

							for (int itemIndex = 0; itemIndex < items.length(); itemIndex++) {
								JSONObject item = items.getJSONObject(itemIndex);
																
								Notice notice = new Notice(NoticeType.NOTICE);
								notice.Id = item.has("id") ? item.getLong("id") : null;
								notice.DiscussionId = item.has("id_klub") ? item.getLong("id_klub") : null;
								notice.WriteupId = item.has("id_wu") ? item.getLong("id_wu") : null;
								notice.CommentId = item.has("id_comment") ? item.getLong("id_comment") : null;
								notice.Section = item.getString("section");
								notice.Time = item.getLong("time");
								notice.Nick = item.getString("nick");
								notice.Content = (item.has("content") && !item.isNull("content")) ? item.getString("content") : "";
								results.add(notice);

								if (item.has("thumbs_up") && !item.isNull("thumbs_up")) {
									Notice thumbsup = new Notice(NoticeType.THUMBS);
									thumbsup.DiscussionId = notice.DiscussionId;
									thumbsup.WriteupId = notice.WriteupId;
									thumbsup.Thumbs = item.getJSONArray("thumbs_up").length();
									results.add(thumbsup);
								}

								if (item.has("replies") && !item.isNull("replies")) {
									JSONArray replies = item.getJSONArray("replies");

									for (int replyIndex = 0; replyIndex < replies.length(); replyIndex++) {
										JSONObject replyJson = replies.getJSONObject(replyIndex);

										Notice reply = new Notice(NoticeType.REPLY);
										reply.Id = replyJson.has("id") ? replyJson.getLong("id") : null;
										reply.DiscussionId = replyJson.has("id_klub") ? replyJson.getLong("id_klub") : null;
										reply.WriteupId = replyJson.has("id_wu") ? replyJson.getLong("id_wu") : null;
										reply.CommentId = replyJson.has("id_comment") ? replyJson.getLong("id_comment") : null;
										reply.Section = notice.Section; // use parents section
										reply.Time = replyJson.getLong("time");
										reply.Nick = replyJson.getString("nick");
										reply.Content = (replyJson.has("text") && !replyJson.isNull("text")) ? replyJson.getString("text") : "";
										reply.IsNew = lastVisit < reply.Time;
										results.add(reply);
									}
								}
							}
						}
					}
				} catch (JSONException e) {
					log.error("GetNoticesTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return results;
		}
	}
}
