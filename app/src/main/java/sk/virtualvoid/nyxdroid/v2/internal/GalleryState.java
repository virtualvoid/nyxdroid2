package sk.virtualvoid.nyxdroid.v2.internal;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;

/** Keeps the growing URL queue in memory, not in a size-limited saved-state Bundle. */
public class GalleryState extends ViewModel {
    public final ArrayList<Bundle> images = new ArrayList<>();
    public long discussionId;
    public long lastWriteupId;
    public String filterUser;
    public String filterContents;
    public int position;
    public boolean hasMore;
    public boolean loadFailed;
    public boolean initialized;

    public boolean shouldLoadMore() {
        return hasMore && !loadFailed && position >= images.size() - 3;
    }

    public void appendOlderPosts(List<Writeup> posts) {
        long previousCursor = lastWriteupId;
        long nextCursor = previousCursor;
        ArrayList<Bundle> added = new ArrayList<>();
        HashSet<Long> seen = new HashSet<>();
        ArrayList<Writeup> ordered = new ArrayList<>(posts);
        Collections.sort(ordered, (left, right) -> Long.compare(right.Id, left.Id));

        for (Writeup post : ordered) {
            // The existing API query includes its boundary post; it is already in the queue.
            if (post.Id <= 0 || post.Id >= previousCursor || !seen.add(post.Id)) {
                continue;
            }
            nextCursor = Math.min(nextCursor, post.Id);
            for (Bundle image : post.allImages()) {
                String url = image.getString(Constants.KEY_URL);
                if (url != null && !url.isEmpty()) {
                    image.putString(Constants.KEY_URL, Constants.fixAttachmentUrl(url));
                    added.add(image);
                }
            }
        }

        images.addAll(added);
        lastWriteupId = nextCursor;
        // Text-only pages advance the cursor too. Only an empty/non-progressing page ends it.
        hasMore = nextCursor < previousCursor;
    }
}
