package sk.virtualvoid.core;

import sk.virtualvoid.core.widgets.CustomUrlSpan;
import sk.virtualvoid.nyxdroid.library.Constants;

import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;

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
        Pattern discussionPtr = Pattern.compile(".*/discussion/(\\d+)/id/(\\d+)", Pattern.CASE_INSENSITIVE);

        URLSpan[] urlSpans = input.getSpans(0, input.length(), URLSpan.class);
        for (URLSpan span : urlSpans) {
            int start = input.getSpanStart(span);
            int end = input.getSpanEnd(span);
            int flags = input.getSpanFlags(span);

            String spanUrl = span.getURL();
            Matcher discussionMatch = discussionPtr.matcher(spanUrl);
            if (discussionMatch.matches()) {
                // discussion id at pos 1
                long discussionId = Long.parseLong(discussionMatch.group(1));

                // discussion post id at pos 2
                Long postId = null;
                if (discussionMatch.groupCount() == 2) {
                    postId = Long.parseLong(discussionMatch.group(2));
                }

                ((Spannable) input).removeSpan(span);

                CustomUrlSpan replacement = new CustomUrlSpan(span.getURL(), discussionId, postId);

                ((Spannable) input).setSpan(replacement, start, end, flags);
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
}
