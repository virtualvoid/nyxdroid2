package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.util.ArrayList;

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
import sk.virtualvoid.nyxdroid.v2.data.Bookmark;
import sk.virtualvoid.nyxdroid.v2.data.BookmarkCategory;
import sk.virtualvoid.nyxdroid.v2.data.query.BookmarkQuery;

import android.app.Activity;

/**
 * @author Juraj
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

    private static Bookmark convert(JSONObject bookmark, JSONObject category) throws JSONException {
        Bookmark result = new Bookmark();
        result.Id = bookmark.getLong("discussion_id");
        result.CategoryId = category.getLong("id");
        result.Name = bookmark.getString("full_name");

        if (bookmark.has("new_posts_count")) {
            result.Unread = bookmark.getInt("new_posts_count"); // TODO: new_links_count, new_images_count
        }
        // TODO: last_visited_at

//        if (bookmark.has("replies") && !bookmark.isNull("replies")) {
//            result.Replies = bookmark.getInt("replies");
//        }

        return result;
    }

    private static BookmarkCategory convert(JSONObject category) throws JSONException {
        BookmarkCategory result = new BookmarkCategory();
        result.Id = category.getLong("id");
        result.Name = category.getString("category_name");
        return result;
    }

    public static class GetBookmarkCategoriesTaskWorker extends TaskWorker<ITaskQuery, ArrayList<BookmarkCategory>> {
        @Override
        public ArrayList<BookmarkCategory> doWork(ITaskQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }

    public static class GetBookmarksTaskWorker extends TaskWorker<BookmarkQuery, ArrayList<Bookmark>> {
        @Override
        public ArrayList<Bookmark> doWork(BookmarkQuery input) throws NyxException {
            ArrayList<Bookmark> resultList = new ArrayList<Bookmark>();

            Connector connector = new Connector(getContext());

            JSONObject json = connector.get("/bookmarks" + (input.IncludeUnread ? "/all" : ""));
            if (json == null) {
                throw new NyxException("Json result was null ?");
            } else {
                try {
                    JSONArray rootmarks = json.getJSONArray("bookmarks");
                    for (int rootMarkIndex = 0; rootMarkIndex < rootmarks.length(); rootMarkIndex++) {
                        JSONObject rootmark = rootmarks.getJSONObject(rootMarkIndex);

                        JSONObject category = rootmark.getJSONObject("category");
                        resultList.add(convert(category));

                        JSONArray bookmarksInCategory = rootmark.getJSONArray("bookmarks");
                        for (int bookmarkInCategoryIndex = 0; bookmarkInCategoryIndex < bookmarksInCategory.length(); bookmarkInCategoryIndex++) {
                            JSONObject bookmark = bookmarksInCategory.getJSONObject(bookmarkInCategoryIndex);
                            resultList.add(convert(bookmark, category));
                        }
                    }
                } catch (Throwable e) {
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
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }

    public static class GetMovementTaskWorker extends TaskWorker<BookmarkQuery, ArrayList<Bookmark>> {
        @Override
        public ArrayList<Bookmark> doWork(BookmarkQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }

    public static class GetBookmarksInCategoryTaskWorker extends TaskWorker<BookmarkQuery, ArrayList<Bookmark>> {
        @Override
        public ArrayList<Bookmark> doWork(BookmarkQuery input) throws NyxException {
            throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
        }
    }
}
