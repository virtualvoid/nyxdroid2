package sk.virtualvoid.nyxdroid.v2.data.dac;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.net.Error;
import sk.virtualvoid.net.IConnector;
import sk.virtualvoid.net.JSONObjectResult;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Attachment;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;

public final class AttachmentDataAccess {
    private AttachmentDataAccess() {}

    public static NullResponse send(Context context, IConnector connector, List<Attachment> attachments,
            String fileType, long specificId, String url, HashMap<String, String> form) throws NyxException {
        ArrayList<Long> uploadedIds = new ArrayList<>();
        boolean sendStarted = false;
        try {
            for (Attachment attachment : attachments) {
                checkCancelled();
                HashMap<String, Object> upload = new HashMap<>();
                upload.put("file", attachment.getMultipartPart(context));
                upload.put("file_type", fileType);
                upload.put("id_specific", specificId);
                JSONObjectResult result = connector.multipart("/file/upload", upload);
                requireSuccess(result);
                try {
                    uploadedIds.add(result.getJson().getLong("id"));
                } catch (Exception e) {
                    throw new NyxException(e);
                }
            }
            checkCancelled();
            sendStarted = true;
            JSONObjectResult result = connector.form(url, form);
            // A missing response can mean the post was sent. Do not delete its files.
            if (result != null && !result.isSuccess()) sendStarted = false;
            requireSuccess(result);
            return NullResponse.success();
        } finally {
            if (!sendStarted) {
                boolean interrupted = Thread.interrupted();
                try {
                    for (Long id : uploadedIds) {
                        try {
                            JSONObjectResult result = connector.delete("/file/delete/" + id);
                            if (result == null || !result.isSuccess()) {
                                Log.w(Constants.TAG, "Unable to remove pending attachment " + id);
                            }
                        } catch (Exception e) {
                            Log.w(Constants.TAG, "Unable to remove pending attachment " + id, e);
                        }
                    }
                } finally {
                    if (interrupted) Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static void checkCancelled() throws NyxException {
        if (Thread.currentThread().isInterrupted()) throw new NyxException("Attachment upload cancelled");
    }

    private static void requireSuccess(JSONObjectResult result) throws NyxException {
        if (result == null) throw new NyxException("No response from Nyx");
        if (!result.isSuccess()) {
            Error error = result.getError();
            throw new NyxException(String.format("%s: %s", error.getCode(), error.getMessage()));
        }
    }
}
