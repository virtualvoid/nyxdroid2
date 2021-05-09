package sk.virtualvoid.nyxdroid.library;

import java.nio.charset.Charset;

import sk.virtualvoid.nyxdroid.v2.internal.VotingType;

/**
 * @author juraj
 */
public class Constants {
    public static final String TAG = "nyxdroid";
    public static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");
    public static final String NOT_IMPLEMENTED_YET = "Je to rozbité, treba to opraviť.";
    public static final int MEMORY_TRIM_LEVEL = 80;

    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    public static final String INDEX_WWW = "https://www.nyx.cz";
    public static final String INDEX = "https://www.nyx.cz";

    public static final String AUTH_NICK = "AuthNick";
    public static final String AUTH_TOKEN = "AuthToken";
    public static final String AUTH_CONFIRMED = "AuthConfirmed";
    public static final String FIREBASE_TOKEN_KEY = "FIREBASE_TOKEN";

    public static final String NOTIFICATIONS_ENABLED = "notifications_enabled";

    public static final String REFRESH_MAIL_INTENT_FILTER = "sk.virtualvoid.nyxdroid.v2.REFRESH_MAIL";
    public static final String REFRESH_MAIL_COUNT = "refreshMailCount";
    public static final String REFRESH_NOTICES_INTENT_FILTER = "sk.virtualvoid.nyxdroid.v2.REFRESH_NOTICES";

    public static final String KEY_ID = "id";
    public static final String KEY_NICK = "nick";
    public static final String KEY_BOOKMARKS_IS_HISTORY = "bookmarksIsHistory";
    public static final String KEY_TITLE = "title";
    public static final String KEY_WU_ID = "wu_id";
    public static final String KEY_TIME = "Time";
    public static final String KEY_RATING = "Rating";
    public static final String KEY_URL = "Url";
    public static final String KEY_THUMBNAIL_URL = "ThumbnailUrl";
    public static final String KEY_UNREAD = "Unread";
    public static final String KEY_BUNDLE_ARRAY = "bundleArray";
    public static final String KEY_VOTING_RESULT = "wuVoteResult";
    public static final String KEY_ACTIVITY_COUNT = "baseActivityActivityCount";

    public static final String REQUEST_MAIL = "mail";
    public static final String REQUEST_WRITEUP = "writeup";
    public static final String REQUEST_WRITEUP_DISCUSSION_ID = "writeup_discussion_id";
    public static final String REQUEST_WRITEUP_DISCUSSION_NAME = "writeup_discussion_name";
    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 64;
    public static final int REQUEST = 1;
    public static final int REQUEST_RESPONSE_FAIL = -1;
    public static final int REQUEST_RESPONSE_CANCEL = 0;
    public static final int REQUEST_RESPONSE_OK = 1;
    public static final int REQUEST_RESPONSE_VOTING = 2;
    public static final int REQUEST_ATTACHMENT = 0x1A;
    public static final int REQUEST_GALLERY = 0x9D;

    public static final String FROM = "from";
    public static final String TO = "to";
    public static final int FROM_ID = 0;
    public static final int TO_ID = 1;

    public static final String MYVOTE_POSITIVE = VotingType.POSITIVE.toString();
    public static final String MYVOTE_NEGATIVE = VotingType.NEGATIVE.toString();
    public static final String MYVOTE_NEGATIVE_VISIBLE = VotingType.NEGATIVE_VISIBLE.toString();

    private Constants() {

    }

    public static String getApiUrl() {
        final String result = "https://nyx.cz/api";
        return result;
    }

    public static String fixAttachmentUrl(String url) {
        if (!url.startsWith(Constants.HTTP) && !url.startsWith(Constants.HTTPS)) {
            url = String.format("%s%s", Constants.INDEX_WWW, url);
        }
        return url;
    }

    public static class ImageLoader {
        public static final String FILE_PROTOCOL = "file://";

        public static final int DiscCacheFileCount = 1024;
        public static final int MemoryCachePercentage = 75;
        public static final int MemoryCacheMaxWidth = 1280;
        public static final int MemoryCacheMaxHeight = 1024;
    }

    public enum WriteupDirection {
        WRITEUP_DIRECTION_NEWEST("newest"),
        WRITEUP_DIRECTION_NEWER("newer_than"),
        WRITEUP_DIRECTION_OLDER("older_than"),
        WRITEUP_DIRECTION_OLDEST("oldest");

        private String value;

        private WriteupDirection(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
