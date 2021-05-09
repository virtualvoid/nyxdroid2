package sk.virtualvoid.nyxdroid.v2.data;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.virtualvoid.nyxdroid.library.Constants;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * @author Juraj
 */
public class Writeup extends BaseComposePoco implements Parcelable {
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_POLL = 97;
    public static final int TYPE_LAST = 98;

    private static Pattern ptrSpoilerSearch = Pattern.compile(".*\"spoiler\"", Pattern.CASE_INSENSITIVE);

    public boolean Unread;
    public int Rating;
    public int Type;
    public UserActivity Location;
    public boolean IsSelected;

    public boolean CanDelete;
    public boolean IsReminded;
    public String MyRating;

    public Writeup(int type) {
        Type = type;
    }

    public Writeup(Parcel source) {
        Id = source.readLong();
        Nick = source.readString();
        Time = source.readLong();
        Content = source.readString();
        Unread = source.readByte() == 1;
        Rating = source.readInt();
        Type = source.readInt();
        Location = source.readParcelable(UserActivity.class.getClassLoader());
        IsMine = source.readByte() == 1;
        CanDelete = source.readByte() == 1;
        IsReminded = source.readByte() == 1;
        MyRating = source.readString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Writeup)) {
            return false;
        }

        Writeup other = (Writeup) obj;
        return this.Id == other.Id;
    }

    public static Writeup fromJSONObject(JSONObject post) throws JSONException {
        Writeup writeup = null;
        if (post.has("post_type") && !post.isNull("post_type")) {
            String typeStr = post.getString("post_type");
            if (typeStr.equalsIgnoreCase("poll") && post.has("content_raw") && !post.isNull("content_raw")) {
                JSONObject contentRaw = post.getJSONObject("content_raw");

                if (!contentRaw.getString("type").equalsIgnoreCase("poll") || (!contentRaw.has("data") || contentRaw.isNull("data"))) {
                    return null;
                }

                writeup = Poll.fromJSONObject(contentRaw.getJSONObject("data"));
            }
        }

        if (writeup == null) {
            writeup = new Writeup(Writeup.TYPE_DEFAULT);
        }

        writeup.Id = post.getLong("id");
        writeup.Nick = post.getString("username");
        writeup.Time = BasePoco.timeFromString(post.getString("inserted_at"));
        writeup.Content = post.getString("content");
        writeup.Unread = post.has("new") && post.getBoolean("new");
        writeup.Rating = post.has("rating") ? post.getInt("rating") : 0;
        writeup.Location = UserActivity.fromJson(post);
        writeup.CanDelete = post.has("can_be_deleted") && post.getBoolean("can_be_deleted");
        writeup.IsReminded = post.has("reminder") && post.getBoolean("reminder");
        writeup.MyRating = post.has("my_rating") && !post.isNull("my_rating") ? post.getString("my_rating") : "";

        return writeup;
    }


    public ArrayList<Bundle> allImages() {
        Document doc = Jsoup.parse(Content);
        Elements es = doc.getElementsByTag("img");

        ArrayList<Bundle> results = new ArrayList<Bundle>();

        for (int elementIndex = 0; elementIndex < es.size(); elementIndex++) {
            Element e = es.get(elementIndex);

            String url = e.attr("src");
            String thumbnailUrl = e.attr("data-thumb");

            try {
                Bundle info = new Bundle();
                info.putLong(Constants.KEY_WU_ID, Id);
                info.putString(Constants.KEY_NICK, Nick);
                info.putLong(Constants.KEY_TIME, Time);
                info.putInt(Constants.KEY_RATING, Rating);
                info.putBoolean(Constants.KEY_UNREAD, Unread);
                info.putString(Constants.KEY_URL, URLDecoder.decode(url, Constants.DEFAULT_CHARSET.displayName()));
                info.putString(Constants.KEY_THUMBNAIL_URL, URLDecoder.decode(thumbnailUrl, Constants.DEFAULT_CHARSET.displayName()));

                results.add(info);
            } catch (Throwable t) {

            }
        }

        return results;
    }

    public boolean youtubeFix() {
        // fuck yo java regex
        if (!Content.contains("youtube.com") && !Content.contains("youtu.be")) {
            return false;
        }

        // ideme parsovat vrateny obsah
        // treba najst popis videa, link
        // a obrazky (alebo obrazok)
        // vsetko ostatne zahodit.

        /*
        NECHAT: <b>How to tune ms41 ECUs (BMW m52/s52 engines): Vanos tuning</b>
        NECHAT: <br>
        NECHAT: <a href='https://www.youtube.com/watch?v=LzfCsMxEnnM' class='extlink'>https://www.youtube.com/watch?v=LzfCsMxEnnM</a>
        NECHAT: <br>
        <div class='embed-wrapper' data-embed-type=youtube data-embed-value=LzfCsMxEnnM data-embed-hd=1>
            NECHAT: <img src='https://img.youtube.com/vi/LzfCsMxEnnM/0.jpg' data-thumb='https://nyx.cz/thumbs/e7/63/e7638cb74ca9a8e367618eb4c12b4252.jpg?url=https%3A%2F%2Fimg.youtube.com%2Fvi%2FLzfCsMxEnnM%2F0.jpg'>
            <img class="play sd" src="/images/play.png">
            <img class="play hd" src="/images/play-hd.png">
        </div>
         */
        Document doc = Jsoup.parse(Content);
        Elements bodies = doc.getElementsByTag("body");
        if (bodies.size() != 1) {
            return false;
        }

        StringBuilder newContent = new StringBuilder();

        Element body = bodies.get(0);
        for (int nodeIndex = 0; nodeIndex < body.childNodeSize(); nodeIndex++) {
            Node node = body.childNode(nodeIndex);
            if (node instanceof Element) {
                if (node.nodeName().equalsIgnoreCase("b") || node.nodeName().equalsIgnoreCase("br")) {
                    newContent.append(node.toString());
                    continue;
                }
                if (node.nodeName().equalsIgnoreCase("a") && (node.hasAttr("class") && (node.attr("class").equalsIgnoreCase("extlink") || node.attr("class").equalsIgnoreCase("r")))) {
                    newContent.append(node.toString());
                    continue;
                }
                if (node.nodeName().equalsIgnoreCase("div") && (node.hasAttr("class") && node.attr("class").equalsIgnoreCase("embed-wrapper"))) {
                    for (int wrapperNodeIndex = 0; wrapperNodeIndex < node.childNodeSize(); wrapperNodeIndex++) {
                        Node wrapperNode = node.childNode(wrapperNodeIndex);
                        if (!wrapperNode.nodeName().equalsIgnoreCase("img") || !wrapperNode.hasAttr("src") || !wrapperNode.hasAttr("data-thumb")) {
                            continue;
                        }
                        newContent.append(wrapperNode.toString());
                    }
                    continue;
                }
            } else if (node instanceof TextNode) {
                newContent.append(node.toString());
            }
        }

        Content = newContent.toString();

        return true;
    }

    public Long marketId() {
        return null;
    }

    public boolean spoilerPresent() {
        Matcher m = ptrSpoilerSearch.matcher(Content);
        return m.find();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(Id);
        dest.writeString(Nick);
        dest.writeLong(Time);
        dest.writeString(Content);
        dest.writeByte((byte) (Unread ? 1 : 0));
        dest.writeInt(Rating);
        dest.writeInt(Type);
        dest.writeParcelable(Location, 0);
        dest.writeByte((byte) (IsMine ? 1 : 0));
        dest.writeByte((byte) (CanDelete ? 1 : 0));
        dest.writeByte((byte) (IsReminded ? 1 : 0));
        dest.writeString(MyRating == null ? "" : MyRating);
    }

    /**
     *
     */
    public static final Parcelable.Creator<Writeup> CREATOR = new Parcelable.Creator<Writeup>() {
        @Override
        public Writeup[] newArray(int size) {
            return new Writeup[size];
        }

        @Override
        public Writeup createFromParcel(Parcel source) {
            return new Writeup(source);
        }
    };
}
