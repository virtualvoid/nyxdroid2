package sk.virtualvoid.nyxdroid.v2.data;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import sk.virtualvoid.nyxdroid.v2.data.TypedPoco.Type;

public class Attachment extends BasePoco implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String STATE_ATTACHMENTS = "compose_attachments";

    private final String source;
    private final String fileName;
    private final String mimeType;

    private Attachment(Uri uri, String name, String type) {
        source = uri.toString();
        fileName = name == null || name.isEmpty() ? "attachment" : name.replaceAll("[/\\\\\\p{Cntrl}]", "_");
        if (type == null || MediaType.parse(type) == null) {
            int dot = fileName.lastIndexOf('.');
            type = dot < 0 ? null : MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileName.substring(dot + 1).toLowerCase(Locale.ROOT));
        }
        mimeType = type == null ? "application/octet-stream" : type;
    }

    public static Attachment fromFile(File file) {
        return new Attachment(Uri.fromFile(file), file.getName(), null);
    }

    public static Attachment fromUri(Context context, Uri uri) {
        String name = uri.getLastPathSegment();
        try (Cursor cursor = context.getContentResolver().query(uri,
                new String[] { OpenableColumns.DISPLAY_NAME }, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (column >= 0 && !cursor.isNull(column)) name = cursor.getString(column);
            }
        }
        return new Attachment(uri, name, context.getContentResolver().getType(uri));
    }

    public Uri getUri() {
        return Uri.parse(source);
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isImage() {
        return mimeType.startsWith("image/");
    }

    public MultipartBody.Part getMultipartPart(Context context) {
        RequestBody body = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(mimeType);
            }

            @Override
            public boolean isOneShot() {
                return true;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream input = context.getContentResolver().openInputStream(getUri())) {
                    if (input == null) throw new IOException("Cannot read " + fileName);
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException();
                        sink.write(buffer, 0, read);
                    }
                }
            }
        };
        return MultipartBody.Part.createFormData("file", fileName, body);
    }

    public static ArrayList<Attachment> getSelected(List<TypedPoco<?>> model) {
        ArrayList<Attachment> result = new ArrayList<>();
        for (TypedPoco<?> item : model) {
            if (item.Type == Type.ATTACHMENT) result.add((Attachment) item.ChildPoco);
        }
        return result;
    }

    public static boolean addSelected(Context context, Intent data, List<TypedPoco<?>> model) {
        if (data == null) return true;
        ArrayList<Uri> uris = new ArrayList<>();
        ClipData clip = data.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) uris.add(clip.getItemAt(i).getUri());
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        boolean success = true;
        for (Uri uri : uris) {
            if (uri == null) continue;
            boolean duplicate = false;
            for (Attachment existing : getSelected(model)) {
                if (existing.source.equals(uri.toString())) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) continue;
            try {
                model.add(new TypedPoco<>(Type.ATTACHMENT, fromUri(context, uri)));
            } catch (RuntimeException e) {
                success = false;
            }
        }
        return success;
    }
}
