package sk.virtualvoid.nyxdroid.v2.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.widgets.INavigationSpan;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.adapters.InformedViewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Juraj
 */
public class NavigationHandler {

    private INavigationSpan navigationSpan;
    private Object parentTag;
    private Context context;

    public NavigationHandler(INavigationSpan navigationSpan, Object parentTag, Context context) {
        this.navigationSpan = navigationSpan;
        this.parentTag = parentTag;
        this.context = context;
    }

    public void doNavigation() {
        Log.d(Constants.TAG, String.format("NavigationHandler: %s", navigationSpan.getUrl()));

        if (context instanceof INavigationHandler) {
            if (navigationSpan.isImage()) {
                boolean success = doNavigateImage();
                Log.d(Constants.TAG, "NavigationHandler - image navigation succeeded: " + success);
            } else if (navigationSpan.isNavigation()) {
                boolean success = doNavigateTopic();
                Log.d(Constants.TAG, "NavigationHandler - topic navigation succeeded: " + success);
            } else {
                String url = navigationSpan.getUrl();

                Log.w(Constants.TAG, "NavigationHandler - assuming external resource: " + url);

                if (!CoreUtility.launchBrowser(context, url)) {
                    Toast.makeText(context, R.string.cant_open_it, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean doNavigateImage() {
        boolean result = true;

        //String fullUrl = url.substring(url.lastIndexOf(Constants.WU_IMAGE_PART_FULLURL) + Constants.WU_IMAGE_PART_FULLURL.length());
        Long writeupId = null;

        if (parentTag instanceof InformedViewHolder) {
            writeupId = ((InformedViewHolder) parentTag).getId();
        }

        result = ((INavigationHandler) context).onNavigationRequested(NavigationType.IMAGE, /*fullUrl*/navigationSpan.getUrl(), null, writeupId);

        return result;
    }

    private boolean doNavigateTopic() {
        boolean result = ((INavigationHandler) context).onNavigationRequested(NavigationType.TOPIC, null, navigationSpan.getDiscussionId(), navigationSpan.getPostId());
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
}
