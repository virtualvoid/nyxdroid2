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
import sk.virtualvoid.nyxdroid.v2.data.Bookmark;
import sk.virtualvoid.nyxdroid.v2.data.BookmarkCategory;
import sk.virtualvoid.nyxdroid.v2.data.query.BookmarkQuery;
import android.app.Activity;

/**
 * 
 * @author Juraj
 * 
 */
public class BookmarkDataAccess {
	private final static Logger log = Logger.getLogger(BookmarkDataAccess.class);

	public static Task<BookmarkQuery, ArrayList<Bookmark>> getBookmarks(Activity context, TaskListener<ArrayList<Bookmark>> listener) {
		return new Task<BookmarkQuery, ArrayList<Bookmark>>(context, new GetBookmarksTaskWorker(), listener);
	}

	public static Task<BookmarkQuery, ArrayList<Bookmark>> searchBookmarks(Activity context, TaskListener<ArrayList<Bookmark>> listener) {
		return new Task<BookmarkQuery, ArrayList<Bookmark>>(context, new SearchBookmarksTaskWorker(), listener);
	}

	public static Task<BookmarkQuery, ArrayList<Bookmark>> getMovement(Activity context, TaskListener<ArrayList<Bookmark>> listener) {
		return new Task<BookmarkQuery, ArrayList<Bookmark>>(context, new GetMovementTaskWorker(), listener);
	}

	public static Task<BookmarkQuery, ArrayList<Bookmark>> getInCategory(Activity context, TaskListener<ArrayList<Bookmark>> listener) {
		return new Task<BookmarkQuery, ArrayList<Bookmark>>(context, new GetBookmarksInCategoryTaskWorker(), listener);
	}

	public static Task<ITaskQuery, ArrayList<BookmarkCategory>> getBookmarkCategories(Activity context, TaskListener<ArrayList<BookmarkCategory>> listener) {
		return new Task<ITaskQuery, ArrayList<BookmarkCategory>>(context, new GetBookmarkCategoriesTaskWorker(), listener);
	}

	private static Bookmark convertFull(JSONObject obj) throws JSONException {
		Bookmark bookmark = new Bookmark();
		bookmark.Id = obj.getLong("id_klub");
		bookmark.CategoryId = obj.getLong("id_cat");
		bookmark.Name = obj.getString("jmeno");
		bookmark.Unread = obj.getInt("unread");

		if (obj.has("replies") && !obj.isNull("replies")) {
			bookmark.Replies = obj.getInt("replies");
		}

		return bookmark;
	}

	private static Bookmark convertMinimal(JSONObject obj) throws JSONException {
		Bookmark bookmark = new Bookmark();
		bookmark.Id = obj.getLong("id_klub");
		bookmark.Name = obj.getString("jmeno");

		if (obj.has("unread") && !obj.isNull("unread")) {
			bookmark.Unread = obj.getInt("unread");
		}

		return bookmark;
	}

	private static BookmarkCategory convertCategory(JSONObject obj) throws JSONException {
		BookmarkCategory category = new BookmarkCategory();
		category.Id = obj.getLong("id_cat");
		category.Name = obj.getString("jmeno");
		return category;
	}

	public static class GetBookmarkCategoriesTaskWorker extends TaskWorker<ITaskQuery, ArrayList<BookmarkCategory>> {
		@Override
		public ArrayList<BookmarkCategory> doWork(ITaskQuery input) throws NyxException {
			ArrayList<BookmarkCategory> categoryList = new ArrayList<BookmarkCategory>();

			Connector connector = new Connector(getContext());

			JSONObject json = connector.call("bookmarks", "new", Connector.EmptyParams, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						JSONObject data = json.getJSONObject("data");

						if (data.has("categories") && !data.isNull("categories")) {
							JSONArray categories = data.getJSONArray("categories");
							for (int i = 0; i < categories.length(); i++) {
								BookmarkCategory category = convertCategory(categories.getJSONObject(i));
								// vsetky okrem "vlastni diskuze"
								if (category.Id != -1) {
									categoryList.add(category);
								}
							}

						}
					}
				} catch (JSONException e) {
					log.error("GetBookmarkCategoriesTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return categoryList;
		}
	}

	public static class GetBookmarksTaskWorker extends TaskWorker<BookmarkQuery, ArrayList<Bookmark>> {
		@Override
		public ArrayList<Bookmark> doWork(BookmarkQuery input) throws NyxException {
			ArrayList<Bookmark> resultList = new ArrayList<Bookmark>();
			ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
			ArrayList<BookmarkCategory> categoryList = new ArrayList<BookmarkCategory>();

			Connector connector = new Connector(getContext());

			JSONObject json = connector.call("bookmarks", input.IncludeUnread ? "all" : "new", Connector.EmptyParams, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				try {
					if (!json.isNull("data")) {
						int replyCount = 0;

						JSONObject data = json.getJSONObject("data");
						if (data.has("discussions") && !data.isNull("discussions")) {
							JSONArray discussions = data.getJSONArray("discussions");
							for (int i = 0; i < discussions.length(); i++) {
								Bookmark converted = convertFull(discussions.getJSONObject(i));
								if (converted.Replies > 0) {
									converted.CategoryId = BookmarkCategory.RepliesCategoryId;
									replyCount++;
								}
								bookmarkList.add(converted);
							}
						}

						if (replyCount > 0) {
							categoryList.add(new BookmarkCategory(BookmarkCategory.RepliesCategoryId, "replies"));
						}

						if (data.has("categories") && !data.isNull("categories")) {
							JSONArray categories = data.getJSONArray("categories");
							for (int i = 0; i < categories.length(); i++) {
								categoryList.add(convertCategory(categories.getJSONObject(i)));
							}
						}

						for (BookmarkCategory category : categoryList) {
							resultList.add(category);

							ArrayList<Bookmark> toRemove = new ArrayList<Bookmark>();
							for (Bookmark bookmark : bookmarkList) {
								if (bookmark.CategoryId == category.Id) {
									resultList.add(bookmark);
									toRemove.add(bookmark);
								}
							}
							bookmarkList.removeAll(toRemove);
						}
					}
				} catch (JSONException e) {
					log.error("GetBookmarksTaskWorker", e);
					throw new NyxException(e);
				}
			}

			return resultList;
		}
	}

	public static class SearchBookmarksTaskWorker extends TaskWorker<BookmarkQuery, ArrayList<Bookmark>> {
		@Override
		public ArrayList<Bookmark> doWork(BookmarkQuery input) throws NyxException {
			ArrayList<Bookmark> resultList = new ArrayList<Bookmark>();
			try {
				Connector connector = new Connector(getContext());

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("term", input.SearchTerm);

				JSONObject json = connector.call("search", "discussions", params, this);
				if (json == null) {
					throw new NyxException("Json result was null ?");
				} else {
					if (!json.isNull("data")) {
						JSONObject data = json.getJSONObject("data");
						if (data.has("discussions") && !data.isNull("discussions")) {
							JSONArray discussions = data.getJSONArray("discussions");
							for (int i = 0; i < discussions.length(); i++) {
								JSONObject obj = discussions.getJSONObject(i);
								resultList.add(convertMinimal(obj));
							}
						}
					}
				}
			} catch (JSONException e) {
				log.error("SearchBookmarksTaskWorker", e);
				throw new NyxException(e);
			}
			return resultList;
		}
	}

	public static class GetMovementTaskWorker extends TaskWorker<BookmarkQuery, ArrayList<Bookmark>> {
		@Override
		public ArrayList<Bookmark> doWork(BookmarkQuery input) throws NyxException {
			ArrayList<Bookmark> resultList = new ArrayList<Bookmark>();
			try {
				Connector connector = new Connector(getContext());

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("more_results", "1");

				JSONObject json = connector.call("bookmarks", "history", params, this);
				if (json == null) {
					throw new NyxException("Json result was null ?");
				} else {
					if (!json.isNull("data")) {
						JSONObject data = json.getJSONObject("data");
						if (data.has("discussions") && !data.isNull("discussions")) {
							JSONArray discussions = data.getJSONArray("discussions");
							for (int i = 0; i < discussions.length(); i++) {
								JSONObject obj = discussions.getJSONObject(i);
								Bookmark bookmark = convertMinimal(obj);
								if (input.IncludeUnread) {
									resultList.add(bookmark);
								} else if (bookmark.Unread > 0) {
									resultList.add(bookmark);
								}
							}
						}
					}
				}
			} catch (JSONException e) {
				log.error("GetMovementTaskWorker", e);
				throw new NyxException(e);
			}
			return resultList;
		}
	}

	public static class GetBookmarksInCategoryTaskWorker extends TaskWorker<BookmarkQuery, ArrayList<Bookmark>> {
		@Override
		public ArrayList<Bookmark> doWork(BookmarkQuery input) throws NyxException {
			ArrayList<Bookmark> resultList = new ArrayList<Bookmark>();
			try {
				Connector connector = new Connector(getContext());

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("cat", Long.toString(input.CategoryId));

				JSONObject json = connector.call("bookmarks", "cat", params, this);
				if (json == null) {
					throw new NyxException("Json result was null ?");
				} else {
					if (!json.isNull("data")) {
						JSONObject data = json.getJSONObject("data");
						if (data.has("discussions") && !data.isNull("discussions")) {
							JSONArray discussions = data.getJSONArray("discussions");
							for (int i = 0; i < discussions.length(); i++) {
								JSONObject obj = discussions.getJSONObject(i);
								Bookmark bookmark = convertFull(obj);
								if (bookmark.CategoryId == input.CategoryId) {
									resultList.add(bookmark);
								}
							}
						}
					}
				}
			} catch (JSONException e) {
				log.error("GetBookmarksInCategoryTaskWorker", e);
				throw new NyxException(e);
			}
			return resultList;
		}
	}
}
