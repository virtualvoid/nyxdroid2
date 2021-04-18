package sk.virtualvoid.core;

import sk.virtualvoid.core.widgets.CustomUrlSpan;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.NyxdroidApplication;

import android.graphics.Rect;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Juraj
 */
public class CustomHtml {
    public static Spanned fromHtml(String source) {
        return fromHtml(source, null);
    }

    public static Spanned fromHtml(String source, Html.ImageGetter imageGetter) {
        Spanned spanned = android.text.Html.fromHtml(source, imageGetter, null);
        return correctLinkPaths(spanned);
    }

    public static Spanned correctLinkPaths(Spanned input) {
        Pattern discussionPostPtr = Pattern.compile(".*/discussion/(\\d+)/id/(\\d+)", Pattern.CASE_INSENSITIVE);
        Pattern discussionPtr = Pattern.compile(".*/discussion/(\\d+)", Pattern.CASE_INSENSITIVE);
        Pattern attachmentPtr = Pattern.compile(".*(original.bin\\?name)=(\\w+\\.\\w+)", Pattern.CASE_INSENSITIVE);

        URLSpan[] urlSpans = input.getSpans(0, input.length(), URLSpan.class);
        for (URLSpan span : urlSpans) {
            int start = input.getSpanStart(span);
            int end = input.getSpanEnd(span);
            int flags = input.getSpanFlags(span);

            if (createCustomUrlSpan(input, span, start, end, flags, discussionPostPtr)) {
                Log.i(Constants.TAG, String.format("correctLinkPaths: ok: %s", span.getURL()));
            } else if (createCustomUrlSpan(input, span, start, end, flags, discussionPtr)) {
                Log.i(Constants.TAG, String.format("correctLinkPaths: ok: %s", span.getURL()));
            } else if (createCustomAttachmentUrlSpan(input, span, start, end, flags, attachmentPtr)) {
                Log.i(Constants.TAG, String.format("correctLinkPaths: ok: %s", span.getURL()));
            } else {
                Log.e(Constants.TAG, String.format("correctLinkPaths: failed: %s", span.getURL()));
            }
        }

        ImageSpan[] imgSpans = input.getSpans(0, input.length(), ImageSpan.class);
        for (ImageSpan span : imgSpans) {
            int start = input.getSpanStart(span);
            int end = input.getSpanEnd(span);
            int flags = input.getSpanFlags(span);

            ClickableSpan[] tempSpans = input.getSpans(start, end, ClickableSpan.class);
            for (ClickableSpan tempSpan : tempSpans) {
                ((Spannable) input).removeSpan(tempSpan);
            }

            CustomUrlSpan replacement = new CustomUrlSpan(span.getSource(), true);

            ((Spannable) input).setSpan(replacement, start, end, flags);
        }
        return input;
    }

    private static boolean createCustomUrlSpan(Spanned input, URLSpan span, int start, int end, int flags, Pattern ptr) {
        Matcher matcher = ptr.matcher(span.getURL());
        if (matcher.matches()) {
            // discussion id at pos 1
            long discussionId = Long.parseLong(matcher.group(1));

            // discussion post id at pos 2
            Long postId = null;
            if (matcher.groupCount() == 2) {
                postId = Long.parseLong(matcher.group(2));
            }

            ((Spannable) input).removeSpan(span);

            CustomUrlSpan replacement = new CustomUrlSpan(span.getURL(), discussionId, postId);

            ((Spannable) input).setSpan(replacement, start, end, flags);

            return true;
        }
        return false;
    }

    private static boolean createCustomAttachmentUrlSpan(Spanned input, URLSpan span, int start, int end, int flags, Pattern ptr) {
        Matcher matcher = ptr.matcher(span.getURL());
        if (matcher.matches()) {
            ((Spannable) input).removeSpan(span);

            CustomUrlSpan replacement = new CustomUrlSpan(String.format("%s%s", Constants.INDEX, span.getURL()));

            ((Spannable) input).setSpan(replacement, start, end, flags);

            return true;
        }
        return false;
    }
}
