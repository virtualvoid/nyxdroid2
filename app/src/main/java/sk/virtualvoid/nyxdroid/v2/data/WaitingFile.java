package sk.virtualvoid.nyxdroid.v2.data;

import org.json.JSONObject;

import sk.virtualvoid.core.NyxException;

public class WaitingFile {
    private long id;
    private String fileType;
    private String fileName;
    private long size;
    private String mimeType;

    private String url;
    private String thumbUrl;

    private WaitingFile() {

    }

    public static WaitingFile fromJSONObject(JSONObject json) throws NyxException {
        try {
            WaitingFile result = new WaitingFile();
            result.id = json.getLong("id");
            result.fileType = json.getString("file_type");
            result.fileName = json.getString("filename");
            result.size = json.getLong("size");
            result.mimeType = json.getString("mimetype");
            result.url  = json.getString("url");

            if (json.has("thumb_url") && !json.isNull("thumb_url")) {
                result.thumbUrl = json.getString("thumb_url");
            }

            return result;
        } catch (Throwable t) {
            throw new NyxException(t);
        }
    }
}
