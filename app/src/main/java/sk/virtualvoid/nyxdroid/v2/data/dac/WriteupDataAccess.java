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
import sk.virtualvoid.nyxdroid.v2.data.BaseResponse;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.UserActivity;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.data.WriteupBookmarkResponse;
import sk.virtualvoid.nyxdroid.v2.data.WriteupHomeResponse;
import sk.virtualvoid.nyxdroid.v2.data.WriteupResponse;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupBookmarkQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import sk.virtualvoid.nyxdroid.v2.internal.VotingInfoResult;
import sk.virtualvoid.nyxdroid.v2.internal.VotingResponse;
import sk.virtualvoid.nyxdroid.v2.internal.VotingResult;
import sk.virtualvoid.nyxdroid.v2.internal.WriteupBookmarkQueryType;
import android.app.Activity;

/**
 * 
 * @author Juraj
 * 
 */
public class WriteupDataAccess {
	private final static Logger log = Logger.getLogger(WriteupDataAccess.class);

	public static Task<WriteupQuery, WriteupResponse> getWriteups(Activity context, TaskListener<WriteupResponse> listener) {
		return new Task<WriteupQuery, WriteupResponse>(context, new GetWriteupsTaskWorker(), listener);
	}

	public static Task<WriteupQuery, NullResponse> sendWriteup(Activity context, TaskListener<NullResponse> listener) {
		return new Task<WriteupQuery, NullResponse>(context, new SendWriteupTaskWorker(), listener);
	}

	public static Task<WriteupQuery, VotingResponse> giveRating(Activity context, TaskListener<VotingResponse> listener) {
		return new Task<WriteupQuery, VotingResponse>(context, new RateWriteupTaskWorker(), listener);
	}

	public static Task<WriteupQuery, VotingInfoResult> getRatingInfo(Activity context, TaskListener<VotingInfoResult> listener) {
		return new Task<WriteupQuery, VotingInfoResult>(context, new RatingOverviewTaskWorker(), listener);
	}

	public static Task<WriteupQuery, NullResponse> reminder(Activity context, TaskListener<NullResponse> listener) {
		return new Task<WriteupQuery, NullResponse>(context, new ReminderTaskWorker(), listener);
	}

	public static Task<WriteupQuery, NullResponse> delete(Activity context, TaskListener<NullResponse> listener) {
		return new Task<WriteupQuery, NullResponse>(context, new DeleteTaskWorker(), listener);
	}

	public static Task<WriteupQuery, WriteupHomeResponse> getHome(Activity context, TaskListener<WriteupHomeResponse> listener) {
		return new Task<WriteupQuery, WriteupHomeResponse>(context, new GetHomeTaskWorker(), listener);
	}

	public static Task<WriteupBookmarkQuery, WriteupBookmarkResponse> bookOrUnbookWriteup(Activity context, TaskListener<WriteupBookmarkResponse> listener) {
		return new Task<WriteupBookmarkQuery, WriteupBookmarkResponse>(context, new BookOrUnbookWriteupTaskWorker(), listener);
	}

	private static Writeup convert(JSONObject obj) throws JSONException {
		Writeup wu = new Writeup();

		wu.Id = obj.getLong("id_wu");
		wu.Nick = obj.getString("nick");
		wu.Time = obj.getLong("time");
		wu.Content = obj.getString("content");
		wu.Unread = obj.has("new");
		wu.Rating = (obj.has("wu_rating") && !obj.isNull("wu_rating")) ? obj.getInt("wu_rating") : 0;
		// 0 - standard (asi), 97 - market
		wu.Type = (obj.has("wu_type") && !obj.isNull("wu_type")) ? obj.getInt("wu_type") : 0;

		wu.Location = UserActivity.fromJson(obj);

		return wu;
	}

	public static class GetWriteupsTaskWorker extends TaskWorker<WriteupQuery, WriteupResponse> {
		@Override
		public WriteupResponse doWork(WriteupQuery input) throws NyxException {
			WriteupResponse response = new WriteupResponse();
			ArrayList<Writeup> writeupList = new ArrayList<Writeup>();

			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			params.put("id", Long.toString(input.Id));
			params.put("direction", input.Direction.toString());

			if (input.LastId != null) {
				params.put("id_wu", Long.toString(input.LastId + 1));
			} else {
				if (input.isFilterUser() || input.isFilterContents()) {
					params.put("id_wu", "0");
				}

				if (input.isFilterUser()) {
					params.put("filter_user", input.FilterUser);
				}

				if (input.isFilterContents()) {
					params.put("filter_text", input.FilterContents);
				}
			}

			JSONObject json = connector.call("discussion", "messages", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						JSONArray array = json.getJSONArray("data");
						for (int i = 0; i < array.length(); i++) {
							JSONObject object = array.getJSONObject(i);
							writeupList.add(BaseDataAccess.isMine(connector, convert(object)));
						}
					}
				} catch (JSONException e) {
					log.error("GetWriteupsTaskWorker-messages", e);
					throw new NyxException(e);
				}
			}

			response.Writeups = writeupList;

			if (!json.has("discussion") || json.isNull("discussion")) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					JSONObject obj = json.getJSONObject("discussion");

					response.Id = obj.getLong("id_klub");
					response.Name = obj.getString("name");
					response.Booked = obj.has("booked") && !obj.isNull("booked") && obj.getInt("booked") == 1;
					response.Owner = obj.getInt("owner") == 1;

					if (obj.has("rights") && !obj.isNull("rights")) {
						obj = obj.getJSONObject("rights");

						response.CanWrite = obj.getInt("write") == 1;
						response.CanDelete = obj.getInt("delete") == 1;
					}
				} catch (JSONException e) {
					log.error("GetWriteupsTaskWorker-discussion", e);
					throw new NyxException(e);
				}
			}

			return response;
		}
	}

	public static class SendWriteupTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
		@Override
		public NullResponse doWork(WriteupQuery input) throws NyxException {
			NullResponse result = new NullResponse();

			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			params.put("id", Long.toString(input.Id));
			params.put("message", input.Contents);

			if (input.AttachmentSource != null) {
				params.put("attachment", input.AttachmentSource);
			}

			JSONObject json = connector.call("discussion", "send", params, this);
			if (json == null || !json.has("result") || json.isNull("result")) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					String strResult = json.getString("result");
					result.Success = strResult != null && strResult.equalsIgnoreCase(Constants.OK);
				} catch (JSONException e) {
					log.error("SendWriteupTaskWorker", e);
					throw new NyxException(e);
				}
			}
			return result;
		}
	}

	public static class RateWriteupTaskWorker extends TaskWorker<WriteupQuery, VotingResponse> {
		@Override
		public VotingResponse doWork(WriteupQuery input) throws NyxException {
			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			params.put("id_klub", Long.toString(input.Id));
			params.put("id_wu", Long.toString(input.TempId));

			switch (input.VotingType) {
				case POSITIVE:
					params.put("rating", "positive");
					break;
				case NEGATIVE:
					params.put("rating", "negative");
					break;
			}

			if (input.VotingConfirmed) {
				params.put("neg_confirmation", "1");
			}

			if (input.VoteToggle) {
				params.put("toggle", "1");
			}

			VotingResponse response = new VotingResponse();
			response.Result = VotingResult.ERROR;

			JSONObject json = connector.call("discussion", "rating_give", params, this);
			if (json == null || !json.has("result") || json.isNull("result")) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					String result = json.getString("result");
					if (result.equalsIgnoreCase("RATING_GIVEN")) {
						response.Result = VotingResult.RATING_GIVEN;
					}
					if (result.equalsIgnoreCase("RATING_CHANGED")) {
						response.Result = VotingResult.RATING_CHANGED;
					}
					if (result.equalsIgnoreCase("RATING_REMOVED")) {
						response.Result = VotingResult.RATING_REMOVED;
					}
					if (result.equalsIgnoreCase("RATING_NEEDS_CONFIRMATION")) {
						response.Result = VotingResult.RATING_NEEDS_CONFIRMATION;
					}

					if (json.has("current_rating") && !json.isNull("current_rating")) {
						String currentRatingStr = json.getString("current_rating");
						try {
							response.CurrentRating = Integer.parseInt(currentRatingStr);
						} catch (NumberFormatException nfe) {
							log.error(String.format("unable to parse integer from: %s", currentRatingStr), nfe);
						}
					}
				} catch (JSONException e) {
					log.error("RateWriteupTaskWorker", e);
					throw new NyxException(e);
				}
			}
			return response;
		}
	}

	public static class RatingOverviewTaskWorker extends TaskWorker<WriteupQuery, VotingInfoResult> {
		@Override
		public VotingInfoResult doWork(WriteupQuery input) throws NyxException {
			VotingInfoResult result = new VotingInfoResult();

			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			params.put("id_klub", Long.toString(input.Id));
			params.put("id_wu", Long.toString(input.TempId));

			JSONObject json = connector.call("discussion", "rating_info", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					result.Total = json.getInt("total");

					result.Negative = json.getInt("negative");
					JSONArray negativeList = json.getJSONArray("negative_list");
					for (int i = 0; i < negativeList.length(); i++) {
						String who = negativeList.getString(i);
						if (connector.getAuthNick().equalsIgnoreCase(who)) {
							result.MeVotedNegative = true;
						}
						result.NegativeList.add(who);
					}

					result.Positive = json.getInt("positive");
					JSONArray positiveList = json.getJSONArray("positive_list");
					for (int i = 0; i < positiveList.length(); i++) {
						String who = positiveList.getString(i);
						if (connector.getAuthNick().equalsIgnoreCase(who)) {
							result.MeVotedPositive = true;
						}
						result.PositiveList.add(who);
					}
				} catch (JSONException e) {
					log.error("RatingOverviewTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return result;
		}
	}

	public static class ReminderTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
		@Override
		public NullResponse doWork(WriteupQuery input) throws NyxException {
			NullResponse result = new NullResponse();

			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			params.put("id_klub", Long.toString(input.Id));
			params.put("id_wu", Long.toString(input.TempId));
			params.put("reminder", "1");

			JSONObject json = connector.call("discussion", "reminder", params, this);
			if (json == null || !json.has("result") || json.isNull("result")) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					result.Success = json.getString("result").equalsIgnoreCase("ok");
				} catch (JSONException e) {
					log.error("ReminderTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return result;
		}
	}

	public static class DeleteTaskWorker extends TaskWorker<WriteupQuery, NullResponse> {
		@Override
		public NullResponse doWork(WriteupQuery input) throws NyxException {
			NullResponse result = new NullResponse();

			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			params.put("id", Long.toString(input.Id));
			params.put("id_wu", Long.toString(input.TempId));

			JSONObject json = connector.call("discussion", "delete", params, this);
			if (json == null || !json.has("result") || json.isNull("result")) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					result.Success = json.getString("result").equalsIgnoreCase("ok");
				} catch (JSONException e) {
					log.error("DeleteTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return result;
		}
	}

	public static class GetHomeTaskWorker extends TaskWorker<WriteupQuery, WriteupHomeResponse> {
		@Override
		public WriteupHomeResponse doWork(WriteupQuery input) throws NyxException {
			WriteupHomeResponse result = new WriteupHomeResponse();

			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("id_klub", Long.toString(input.Id));

			JSONObject json = connector.call("discussion", "home", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (json.has("header") && !json.isNull("header")) {
						StringBuilder sb = new StringBuilder();

						JSONArray arr = json.getJSONArray("header");
						for (int i = 0; i < arr.length(); i++) {
							sb.append(arr.getString(i).replaceAll("(\r\n|\n)", "<br />"));
						}

						result.Header = sb.toString();
					}

					if (json.has("home") && !json.isNull("home")) {
						StringBuilder sb = new StringBuilder();

						JSONArray arr = json.getJSONArray("home");
						for (int i = 0; i < arr.length(); i++) {
							sb.append(arr.getString(i).replaceAll("(\r\n|\n)", "<br />"));
						}

						result.Home = sb.toString();
					}
				} catch (JSONException e) {
					log.error("GetHomeTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return result;
		}
	}

	public static class BookOrUnbookWriteupTaskWorker extends TaskWorker<WriteupBookmarkQuery, WriteupBookmarkResponse> {
		@Override
		public WriteupBookmarkResponse doWork(WriteupBookmarkQuery input) throws NyxException {
			WriteupBookmarkResponse result = new WriteupBookmarkResponse();

			JSONObject json = null;

			Connector connector = new Connector(getContext());
			HashMap<String, Object> params = new HashMap<String, Object>();

			params.put("id_klub", Long.toString(input.DiscussionId));

			if (input.QueryType == WriteupBookmarkQueryType.BOOK) {
				if (input.CategoryId != null) {
					params.put("category", Long.toString(input.CategoryId));
				}

				json = connector.call("discussion", "book", params, this);
			}

			if (input.QueryType == WriteupBookmarkQueryType.UNBOOK) {
				json = connector.call("discussion", "unbook", params, this);
			}

			if (json == null || !json.has("discussion") || json.isNull("discussion")) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					JSONObject discussion = json.getJSONObject("discussion");

					result.Booked = discussion.has("booked") && !discussion.isNull("booked") && discussion.getInt("booked") == 1;
					result.Success = true;
				} catch (JSONException e) {
					log.error("ReminderTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return result;
		}
	}
}
