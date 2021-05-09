package sk.virtualvoid.net;

public abstract class JSONResult<TJson> implements IJSONResult {
    public static final Integer SUCCESS_CODE = 200;

    private final int statusCode;
    private final TJson json;

    protected JSONResult(int statusCode, TJson json) {
        this.statusCode = statusCode;
        this.json = json;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public TJson getJson() {
        return json;
    }

    public boolean isSuccess() {
        return statusCode == SUCCESS_CODE || statusCode == 204;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }
}
