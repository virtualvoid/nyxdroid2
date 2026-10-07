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
    private static final Pattern VIDEO_TAG_PATTERN = Pattern.compile("<video\\b[^>]*>.*?</video>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SRC_ATTRIBUTE_PATTERN = Pattern.compile("\\bsrc\\s*=\\s*['\"]?([^'\"\\s>]+)", Pattern.CASE_INSENSITIVE);

    public static Spanned fromHtml(String source) {
        return fromHtml(source, null);
    }

    public static Spanned fromHtml(String source, Html.ImageGetter imageGetter) {
        source = replaceVideoTags(source);
        Spanned spanned = android.text.Html.fromHtml(source, imageGetter, null);
        return correctLinkPaths(spanned);
    }

    public static Spanned correctLinkPaths(Spanned input) {
        Pattern discussionPostPtr = Pattern.compile(".*/discussion/(\\d+)/id/(\\d+)", Pattern.CASE_INSENSITIVE);
        Pattern discussionPtr = Pattern.compile(".*/discussion/(\\d+)", Pattern.CASE_INSENSITIVE);
        Pattern attachmentPtr = Pattern.compile(".*(original)\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$", Pattern.CASE_INSENSITIVE);
        Pattern mailReplyPtr = Pattern.compile(".*/mail/id/(\\d+)", Pattern.CASE_INSENSITIVE);

        URLSpan[] urlSpans = input.getSpans(0, input.length(), URLSpan.class);
        for (URLSpan span : urlSpans) {
            int start = input.getSpanStart(span);
            int end = input.getSpanEnd(span);
            int flags = input.getSpanFlags(span);

            if (createCustomAttachmentUrlSpan(input, span, start, end, flags, attachmentPtr)) {
                Log.i(Constants.TAG, String.format("correctLinkPaths: ok: %s", span.getURL()));
            } else if (createCustomUrlSpan(input, span, start, end, flags, discussionPtr)) {
                Log.i(Constants.TAG, String.format("correctLinkPaths: ok: %s", span.getURL()));
            } else if (createCustomMailReplyUrlSpan(input, span, start, end, flags, mailReplyPtr)) {
                Log.i(Constants.TAG, String.format("correctLinkPaths: ok: %s", span.getURL()));
            } else {
                String resolvedUrl = createGenericUrlSpan(input, span, start, end, flags);
                Log.e(Constants.TAG, String.format("correctLinkPaths failed, assuming external link: %s", resolvedUrl));
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

            String source = span.getSource();
            if (source == null || source.isEmpty() || (source != null && source.startsWith("data:"))) {
                continue;
            }

            CustomUrlSpan replacement = new CustomUrlSpan(Constants.fixAttachmentUrl(source), true, false, -1);

            ((Spannable) input).setSpan(replacement, start, end, flags);
        }
        return input;
    }

    private static String replaceVideoTags(String source) {
        if (source == null) {
            return "";
        }

        Matcher matcher = VIDEO_TAG_PATTERN.matcher(source);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String videoTag = matcher.group();
            String videoUrl = findSrc(videoTag);
            String replacement = videoUrl != null && !videoUrl.isEmpty()
                    ? String.format("<a href=\"%s\">[video]</a>", videoUrl.replace("\"", "%22"))
                    : "[video]";

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static String findSrc(String html) {
        Matcher matcher = SRC_ATTRIBUTE_PATTERN.matcher(html);
        return matcher.find() ? matcher.group(1) : null;
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

            CustomUrlSpan replacement = new CustomUrlSpan(Constants.fixAttachmentUrl(span.getURL()), true, false, -1);

            ((Spannable) input).setSpan(replacement, start, end, flags);

            return true;
        }
        return false;
    }

    private static boolean createCustomMailReplyUrlSpan(Spanned input, URLSpan span, int start, int end, int flags, Pattern ptr) {
        Matcher matcher = ptr.matcher(span.getURL());
        if (matcher.matches()) {
            ((Spannable) input).removeSpan(span);

            long mailId = Long.parseLong(matcher.group(1));

            CustomUrlSpan replacement = new CustomUrlSpan(Constants.fixMailReplyUrl(span.getURL()), false, true, mailId);

            ((Spannable) input).setSpan(replacement, start, end, flags);

            return true;
        }
        return false;
    }

    private static String createGenericUrlSpan(Spanned input, URLSpan span, int start, int end, int flags) {
        String url = Constants.fixAttachmentUrl(span.getURL());

        ((Spannable) input).removeSpan(span);

        CustomUrlSpan replacement = new CustomUrlSpan(url);

        ((Spannable) input).setSpan(replacement, start, end, flags);

        return url;
    }
}
