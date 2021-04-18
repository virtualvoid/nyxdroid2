package sk.virtualvoid.core;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.NyxdroidApplication;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.internal.Appearance;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import androidx.collection.LruCache;

import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;


/**
 * @author Juraj
 */
public class ImageGetterAsync {
    private final static Logger log = Logger.getLogger(ImageGetterAsync.class);

    private Resources resources;
    private int density;
    private boolean active = true;

    private static Object syncRoot = new Object();
    private static Drawable defaultDrawable;

    private static Integer mDrawableLruMaxMemory;
    private static Integer mDrawableLruMaxSize;
    private static LruCache<String, Drawable> mDrawableLruCache;
    private static Map<String, Void> mPendingSources;

    private static ImageLoader imageLoader;

    /**
     * Inner classes
     */
    static class PendingTask {
        String Source;
        ImageLoadingListener Listener;
    }

    private LinkedList<PendingTask> pendingTasks;

    /**
     * Static construction
     */
    static {
        // used for identifying pending sources (so we won't try to load those
        // which are already in queue)
        mPendingSources = Collections.synchronizedMap(new WeakHashMap<String, Void>());

        // how many memory we should use ?
        Runtime runtime = Runtime.getRuntime();
        mDrawableLruMaxMemory = (int) runtime.maxMemory();
        mDrawableLruMaxSize = (mDrawableLruMaxMemory / 8);

        mDrawableLruCache = new LruCache<String, Drawable>(mDrawableLruMaxSize) {
            @Override
            protected int sizeOf(String key, Drawable value) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) value;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                int byteCount = bitmap.getByteCount();
                return byteCount;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Drawable oldValue, Drawable newValue) {
                Log.d(Constants.TAG, String.format("ImageGetterAsync evicted: %b / %s", key, evicted));
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };

        log.debug(String.format("ImageGetterAsync lru cache construction: maxMem=%d, maxSize=%d", mDrawableLruMaxMemory, mDrawableLruMaxSize));
    }

    /**
     * Get's cached image for html display
     */
    private static final Html.ImageGetter mImageGetterCached = new Html.ImageGetter() {
        public Drawable getDrawable(String source) {
            synchronized (mDrawableLruCache) {
                Drawable drawable = mDrawableLruCache.get(source);
                if (drawable == null) {
                    Log.w(Constants.TAG, String.format("ImageGetterAsync mImageGetterCached: request wasn't found in cache ! source=%s", source));
                    return defaultDrawable;
                }
                return drawable;
            }
        }
    };

    /**
     * Handling of image task post execution
     */
    private static ImageGetterHandler imageGetterTaskHandling = new ImageGetterHandler() {
        @Override
        public boolean onDone(ImageGetterData data, Drawable drawable) {
            String source = data.getSource();

            synchronized (mDrawableLruCache) {
                mDrawableLruCache.put(source, drawable);
            }

            String content = data.getContent();
            int spawnPosition = data.getSpawnPosition();

            WeakReference<TextView> textViewRef = data.getTextViewRef();
            TextView textView = textViewRef.get();

            if (textView == null) {
                log.warn(String.format("ImageGetterHandler.onDone: textView is garbage collected for spawn position: %d, source: %s", spawnPosition, source));
                return false;
            }

            Object tag = textView.getTag();
            boolean isPartiallyValid = tag instanceof Integer;

            if (isPartiallyValid) {
                Integer currentPosition = ((Integer) tag);

                if (spawnPosition == currentPosition) {
                    Spanned compiledContent = CustomHtml.fromHtml(content, mImageGetterCached);
                    textView.setText(compiledContent);
                }
            }

            return true;
        }
    };

    private static class ExtendedImageLoadingListener implements ImageLoadingListener {
        private Resources resources;
        private DisplayMetrics metrics;
        private int density;
        private ImageGetterData imageDownloadData;

        public ExtendedImageLoadingListener(Resources resources, int density, ImageGetterData imageDownloadData) {
            this.resources = resources;
            this.metrics = resources.getDisplayMetrics();
            this.density = density;
            this.imageDownloadData = imageDownloadData;
        }

        @Override
        public void onLoadingCancelled(String url, View view) {
            Log.w(Constants.TAG, "ImageGetterAsync CANCELLED: " + url);
        }

        @Override
        public void onLoadingComplete(String url, View view, Bitmap bitmap) {
            Drawable drawable = new BitmapDrawable(resources, bitmap);

            Rect drawableBounds = drawable.getBounds();

            float left = drawableBounds.left;
            float top = drawableBounds.top;
            float right = drawableBounds.right == 0 ? drawable.getIntrinsicWidth() * density : drawableBounds.right;
            float bottom = drawableBounds.bottom == 0 ? drawable.getIntrinsicHeight() * density : drawableBounds.bottom;

            if (right - left > (float)metrics.widthPixels / 2) {
                float ratio = (right / (float) metrics.widthPixels) * 2;

                right = right / ratio;
                bottom = bottom / ratio;
            }

            drawable.setBounds((int)left, (int)top, (int)right, (int)bottom);

            imageGetterTaskHandling.onDone(imageDownloadData, drawable);
            mPendingSources.remove(url);

            Log.i(Constants.TAG, "ImageGetterAsync DONE: " + url);
        }

        @Override
        public void onLoadingFailed(String url, View view, FailReason failReason) {
            Log.w(Constants.TAG, "ImageGetterAsync FAILED: " + url);
        }

        @Override
        public void onLoadingStarted(String url, View view) {
            Log.d(Constants.TAG, "ImageGetterAsync STARTED: " + url);
        }
    }

    /**
     * Construction
     *
     * @param context
     */
    public ImageGetterAsync(Activity context) {
        active = true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean scaleImagesByDPI = prefs.getBoolean("scale_images_bydpi", true);

        resources = context.getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();
        density = scaleImagesByDPI ? (int) (metrics.density) : 1;

        pendingTasks = new LinkedList<PendingTask>();

        Appearance appearance = Appearance.getAppearance(context);
        if (appearance.getUseDarkTheme()) {
            defaultDrawable = resources.getDrawable(R.drawable.empty_photo_inv);
        } else {
            defaultDrawable = resources.getDrawable(R.drawable.empty_photo);
        }

        defaultDrawable.setBounds(0, 0, defaultDrawable.getIntrinsicWidth(), defaultDrawable.getIntrinsicHeight());

        imageLoader = ImageLoader.getInstance();
    }

    /**
     * @param spawnPosition
     * @param message
     * @param textView
     * @return
     */
    public Html.ImageGetter spawn(int spawnPosition, String message, TextView textView) {
        final int _position = spawnPosition;
        final String _message = message;
        final TextView _textView = textView;

        return new Html.ImageGetter() {
            public Drawable getDrawable(String source) {
                if (source == null || source.isEmpty()) {
                    log.info(String.format("ImageGetterAsync spawn: got weird source as url: %s in message: %s", source, _message));
                    return defaultDrawable;
                }

                Drawable drawable = mDrawableLruCache.get(source);
                if (drawable != null) {
                    log.info(String.format("ImageGetterAsync spawn: request wasn't found in cache ! source=%s", source));
                    return drawable;
                }

                if (!mPendingSources.containsKey(source)) {
                    final ImageGetterData imageDownloadData = new ImageGetterData(_position, source, _message, _textView);
                    final ImageLoadingListener imageDownloadListener = new ExtendedImageLoadingListener(resources, density, imageDownloadData);

                    if (active) {
                        mPendingSources.put(source, null);

                        imageLoader.loadImage(source, imageDownloadListener);
                    } else {
                        synchronized (syncRoot) {
                            PendingTask pendingTask = new PendingTask();
                            pendingTask.Source = source;
                            pendingTask.Listener = imageDownloadListener;

                            pendingTasks.add(pendingTask);
                        }
                    }
                } else {
                    Log.w(Constants.TAG, String.format("ImageGetterAsync spawn: already in queue: %s", source));
                }

                return defaultDrawable;
            }
        };
    }

    /**
     * @param nowActive
     */
    public void setActive(boolean nowActive) {
        synchronized (syncRoot) {
            if (!active && nowActive) {
                loadPendingImages();
            }
        }
        active = nowActive;
    }

    private void loadPendingImages() {
        PendingTask pendingTask = pendingTasks.pollLast();
        while (pendingTask != null) {
            mPendingSources.put(pendingTask.Source, null);

            imageLoader.loadImage(pendingTask.Source, pendingTask.Listener);
            pendingTask = pendingTasks.pollLast();
        }
    }

    /**
     *
     */
    public static void clearDrawableCache() {
        if (mDrawableLruCache != null) {
            mDrawableLruCache.evictAll();
        }

        if (imageLoader != null) {
            imageLoader.clearMemoryCache();
        }

        if (mPendingSources != null) {
            mPendingSources.clear();
        }

        Log.d(Constants.TAG, "ImageGetterAsync clearDrawablaCache");
    }
}
