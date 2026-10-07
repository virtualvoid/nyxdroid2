package sk.virtualvoid.nyxdroid.v2.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.nyxdroid.library.Constants;

public class GalleryImageSaver extends TaskWorker<ITaskQuery, String> {
    private static final String DIRECTORY = Environment.DIRECTORY_PICTURES + "/Nyxdroid/";
    private final Context applicationContext;
    private final String url;

    public GalleryImageSaver(Context context, String url) {
        applicationContext = context.getApplicationContext();
        this.url = url;
    }

    @Override
    public String doWork(ITaskQuery input) throws NyxException {
        FutureTarget<File> download = Glide.with(applicationContext).load(url)
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        try {
            File source = download.get(60, TimeUnit.SECONDS);
            if (source.length() == 0) {
                throw new IOException("Downloaded image is empty.");
            }
            Uri uri = Uri.parse(url);
            String name = uri.getQueryParameter("name");
            if (name == null || name.isEmpty()) {
                name = uri.getLastPathSegment();
            }
            String mimeType = getMimeType(source, name);
            name = getFileName(name, mimeType);
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    ? saveToMediaStore(source, name, mimeType) : saveToPictures(source, name, mimeType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NyxException(e);
        } catch (Exception e) {
            throw new NyxException(e);
        } finally {
            Glide.clear(download);
        }
    }

    private static String getMimeType(File file, String name) throws IOException {
        String mimeType;
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
            input.mark(64);
            byte[] header = new byte[12];
            int count = input.read(header);
            input.reset();
            if (count == 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
                mimeType = "image/webp";
            } else {
                mimeType = URLConnection.guessContentTypeFromStream(input);
            }
        }
        if (mimeType == null && name != null) {
            int dot = name.lastIndexOf('.');
            if (dot >= 0) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(dot + 1).toLowerCase(Locale.ROOT));
            }
        }
        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new IOException("Downloaded file is not a supported image.");
        }
        return mimeType;
    }

    private static String getFileName(String name, String mimeType) {
        if (name == null) {
            name = "";
        }
        name = name.replaceAll("[\\\\/\\p{Cntrl}]", "_").replaceAll("^\\.+", "").trim();
        if (name.isEmpty()) {
            name = "nyxdroid_" + System.currentTimeMillis();
        }
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            String originalExtension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
            if (mimeType.equals(MimeTypeMap.getSingleton().getMimeTypeFromExtension(originalExtension))) {
                extension = originalExtension;
            }
            name = name.substring(0, dot);
        }
        if (name.isEmpty()) {
            name = "nyxdroid_" + System.currentTimeMillis();
        }
        while (name.getBytes(StandardCharsets.UTF_8).length > 180) {
            name = name.substring(0, name.offsetByCodePoints(name.length(), -1));
        }
        return name + (extension == null ? "" : "." + extension);
    }

    private String saveToMediaStore(File source, String name, String mimeType) throws IOException {
        ContentResolver resolver = applicationContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.Media.RELATIVE_PATH, DIRECTORY);
        values.put(MediaStore.Images.Media.IS_PENDING, 1);
        Uri item = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
        if (item == null) {
            throw new IOException("Unable to create image in Pictures.");
        }
        try {
            try (OutputStream output = resolver.openOutputStream(item)) {
                copy(source, output);
            }
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            if (resolver.update(item, values, null, null) != 1) {
                throw new IOException("Unable to publish saved image.");
            }
            // MediaStore can rename the file when the same name already exists.
            try (Cursor cursor = resolver.query(item, new String[] {
                    MediaStore.Images.Media.RELATIVE_PATH, MediaStore.Images.Media.DISPLAY_NAME }, null, null, null)) {
                if (cursor == null || !cursor.moveToFirst()) {
                    throw new IOException("Unable to read saved image path.");
                }
                return cursor.getString(0) + cursor.getString(1);
            }
        } catch (IOException | RuntimeException e) {
            try {
                resolver.delete(item, null, null);
            } catch (RuntimeException cleanupError) {
                Log.w(Constants.TAG, "Unable to remove incomplete saved image", cleanupError);
            }
            throw e;
        }
    }

    private String saveToPictures(File source, String name, String mimeType) throws IOException {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Nyxdroid");
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Unable to create Pictures directory.");
        }
        File destination = new File(directory, name);
        int dot = name.lastIndexOf('.');
        String stem = dot >= 0 ? name.substring(0, dot) : name;
        String suffix = dot >= 0 ? name.substring(dot) : "";
        int index = 1;
        while (!destination.createNewFile()) {
            destination = new File(directory, stem + " (" + index++ + ")" + suffix);
        }
        try {
            try (OutputStream output = new FileOutputStream(destination)) {
                copy(source, output);
            }
            MediaScannerConnection.scanFile(applicationContext, new String[] { destination.getAbsolutePath() }, new String[] { mimeType }, null);
            return destination.getAbsolutePath();
        } catch (IOException | RuntimeException e) {
            if (!destination.delete()) {
                Log.w(Constants.TAG, "Unable to remove incomplete saved image");
            }
            throw e;
        }
    }

    private static void copy(File source, OutputStream output) throws IOException {
        if (output == null) {
            throw new IOException("Unable to open destination image.");
        }
        try (InputStream input = new FileInputStream(source)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedIOException("Image saving cancelled.");
                }
                output.write(buffer, 0, count);
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedIOException("Image saving cancelled.");
            }
        }
    }
}
