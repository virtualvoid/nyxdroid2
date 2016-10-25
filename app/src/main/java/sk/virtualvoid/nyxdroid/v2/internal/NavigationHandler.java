package sk.virtualvoid.nyxdroid.v2.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.adapters.InformedViewHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author Juraj
 * 
 */
public class NavigationHandler {
	private String url;
	private boolean isImage;
	private Object parentTag;
	private Context context;

	public NavigationHandler(String url, boolean isImage, Object parentTag, Context context) {
		this.url = url;
		this.isImage = isImage;
		this.parentTag = parentTag;
		this.context = context;
	}

	public void doNavigation() {
		Log.d(Constants.TAG, String.format("NavigationHandler: %s", url));

		if (context instanceof INavigationHandler) {
			if (isImage) {
				boolean success = doNavigateImage();
				Log.d(Constants.TAG, "NavigationHandler - image navigation succeeded: " + success);
			} else {
				Pattern locationPtr = Pattern.compile("l=(\\w+)");
				Matcher locationMatch = locationPtr.matcher(url);

				if (!locationMatch.find()) {
					Log.w(Constants.TAG, String.format("NavigationHandler doesn't understand: %s leaving decision to parent implementation.", url));
					((INavigationHandler) context).onNavigationRequested(NavigationType.NONE, url, null, null);
					return;
				}

				String locationType = locationMatch.group(0);
				if (locationType.contains("topic")) {
					boolean success = doNavigateTopic();
					Log.d(Constants.TAG, "NavigationHandler - topic navigation succeeded: " + success);
				}
				if (locationType.contains("events")) {
					boolean success = doNavigateEvent();
					Log.d(Constants.TAG, "NavigationHandler - event navigation succeeded: " + success);
				}
				if (locationType.contains("market")) {
					boolean success = doNavigateMarket();
					Log.d(Constants.TAG, "NavigationHandler - market navigation succeeded: " + success);
				}
			}
		}
	}

	private boolean doNavigateImage() {
		boolean result = true;

		String fullUrl = url.substring(url.lastIndexOf(Constants.WU_IMAGE_PART_FULLURL) + Constants.WU_IMAGE_PART_FULLURL.length());
		Long writeupId = null;

		if (parentTag instanceof InformedViewHolder) {
			writeupId = ((InformedViewHolder) parentTag).getId();
		}

		result = ((INavigationHandler) context).onNavigationRequested(NavigationType.IMAGE, fullUrl, null, writeupId);

		return result;
	}

	private boolean doNavigateTopic() {
		boolean result = true;
		boolean includingWriteupId = url.contains(";wu=");

		Pattern topicPtr = includingWriteupId ? Pattern.compile("l=(\\w+);id=(\\d+);wu=(\\d+)") : Pattern.compile("l=(\\w+);id=(\\d+)");
		Matcher topicMatch = topicPtr.matcher(url);

		if (topicMatch.find() && topicMatch.groupCount() != 0) {
			Long discussionId = Long.parseLong(topicMatch.group(2));
			Long writeupId = includingWriteupId ? Long.parseLong(topicMatch.group(3)) : null;

			result = ((INavigationHandler) context).onNavigationRequested(NavigationType.TOPIC, null, discussionId, writeupId);
		} else {
			result = false;
		}

		return result;
	}

	private boolean doNavigateEvent() {
		boolean result = true;
		// l=events;l2=2;id=21927;pr=0
		Pattern eventPtr = Pattern.compile("l=(\\w+);l2=(\\d+);id=(\\d+)");
		Matcher eventMatch = eventPtr.matcher(url);

		if (eventMatch.find() && eventMatch.groupCount() != 0) {
			Long eventId = Long.parseLong(eventMatch.group(3));

			result = ((INavigationHandler) context).onNavigationRequested(NavigationType.EVENT, null, eventId, null);
		} else {
			result = false;
		}

		return result;
	}

	private boolean doNavigateMarket() {
		boolean result = true;
		// l=market;l2=4;id=99432;n=d42f
		Pattern marketPtr = Pattern.compile("l=(\\w+);l2=(\\d+);id=(\\d+)");
		Matcher marketMatch = marketPtr.matcher(url);

		if (marketMatch.find() && marketMatch.groupCount() != 0) {
			Long marketId = Long.parseLong(marketMatch.group(3));

			result = ((INavigationHandler) context).onNavigationRequested(NavigationType.MARKET, null, marketId, null);
		} else {
			result = false;
		}

		return result;
	}

	public static void startNavigateTopic(Activity context, Class<?> writeupsActivityClass, Long discussionId, Long writeupId) {
		Intent intent = new Intent(context, /*WriteupsActivity.class*/ writeupsActivityClass);
		intent.putExtra(Constants.KEY_ID, discussionId);

		if (writeupId != null) {
			intent.putExtra(Constants.KEY_WU_ID, writeupId);
		}

		context.startActivity(intent);
		context.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	public static void startNavigateEvent(Activity context, Class<?> eventActivityClass, long id, Long commentId) {
		Intent intent = new Intent(context, /*EventActivity.class*/ eventActivityClass);
		intent.putExtra(Constants.KEY_ID, id);
		intent.putExtra(Constants.KEY_COMMENT_ID, commentId);

		context.startActivity(intent);
		context.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
	
	public static void startNavigateMarket(Activity context, Class<?> advertActivityClass, long id) {
		Intent intent = new Intent(context, /*AdvertActivity.class*/ advertActivityClass);
		intent.putExtra(Constants.KEY_ID, id);

		context.startActivity(intent);
		context.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
}
