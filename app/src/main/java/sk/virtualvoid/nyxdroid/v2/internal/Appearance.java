package sk.virtualvoid.nyxdroid.v2.internal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class Appearance {
	public static final int DARK_BOOKMARK_UNREAD_COLOR = Color.parseColor("#EEEEEE");
	public static final int DARK_BOOKMARK_READ_COLOR = Color.parseColor("#999999");
	public static final int DARK_BOOKMARK_CATEGORY_ROW = Color.parseColor("#444444");
	public static final int DARK_LINK_COLOR = Color.parseColor("#FFFFFF");
	public static final String DARK_POSITIVE_VOTES = "#CACACA";
	public static final String DARK_NEGATIVE_VOTES = "#ff0000";

	public static final int LIGHT_BOOKMARK_UNREAD_COLOR = Color.parseColor("#000000");
	public static final int LIGHT_BOOKMARK_READ_COLOR = Color.parseColor("#777777");
	public static final int LIGHT_BOOKMARK_CATEGORY_ROW = Color.parseColor("#DDDDDD");
	public static final int LIGHT_LINK_COLOR = -13388315;
	public static final String LIGHT_POSITIVE_VOTES = "#00dd00";
	public static final String LIGHT_NEGATIVE_VOTES = "#dd0000";

	private boolean useDarkTheme;
	private Integer entryUnreadColor;
	private int bookmarkUnreadColor;
	private int bookmarkReadColor;
	private int categoryBkColor;
	private int linkColor;
	private float fontSize;
	private String voteNegativeColor;
	private String votePositiveColor;
	private int bookmarksPadding;

	private Appearance() {
	}

	public static Appearance getAppearance(Activity context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return getAppearance(prefs);
	}

	public static Appearance getAppearance(SharedPreferences prefs) {
		Appearance appearance = new Appearance();

		boolean useDarkTheme = appearance.useDarkTheme = prefs.getBoolean("use_dark_theme", true);
		appearance.fontSize = Float.parseFloat(prefs.getString("font_size", "14"));

		String entryUnreadColor = prefs.getString("unread_appearance", "null");
		if (!entryUnreadColor.equalsIgnoreCase("null")) {
			appearance.entryUnreadColor = Color.parseColor(entryUnreadColor);
		}
		
		appearance.bookmarkUnreadColor = !useDarkTheme ? LIGHT_BOOKMARK_UNREAD_COLOR : DARK_BOOKMARK_UNREAD_COLOR;
		appearance.bookmarkReadColor = !useDarkTheme ? LIGHT_BOOKMARK_READ_COLOR : DARK_BOOKMARK_READ_COLOR;
		appearance.categoryBkColor = !useDarkTheme ? LIGHT_BOOKMARK_CATEGORY_ROW : DARK_BOOKMARK_CATEGORY_ROW;
		appearance.linkColor = !useDarkTheme ? LIGHT_LINK_COLOR : DARK_LINK_COLOR;
		appearance.voteNegativeColor = !useDarkTheme ? LIGHT_NEGATIVE_VOTES : DARK_NEGATIVE_VOTES;
		appearance.votePositiveColor = !useDarkTheme ? LIGHT_POSITIVE_VOTES : DARK_POSITIVE_VOTES;

		appearance.bookmarksPadding = Integer.parseInt(prefs.getString("bookmark_padding", "0"));
		
		return appearance;
	}

	public boolean getUseDarkTheme() {
		return useDarkTheme;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(TextView... textViews) {
		for (TextView textView : textViews) {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
		}
	}

	public Integer getEntryUnreadColor() {
		return entryUnreadColor;
	}
	
	public boolean isEntryUnreadColorStandard() {
		return entryUnreadColor == null;
	}
	
	public int getBookmarkUnreadColor() {
		return bookmarkUnreadColor;
	}

	public int getBookmarkReadColor() {
		return bookmarkReadColor;
	}

	public int getCategoryBackgroundColor() {
		return categoryBkColor;
	}

	public int getLinkColor() {
		return linkColor;
	}

	public String getVoteNegativeColor() {
		return voteNegativeColor;
	}

	public String getVotePositiveColor() {
		return votePositiveColor;
	}
	
	public int getBookmarksPadding() {
		return bookmarksPadding;
	}
}
