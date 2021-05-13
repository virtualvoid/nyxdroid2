package sk.virtualvoid.net;

public abstract class JSONResult<TJson> implements IJSONResult {
    public static final Integer SUCCESS_CODE = 200;
    public static final Integer SUCCESS_NOCONTENT_CODE = 204;
    public static final Integer UNAUTHORIZED_CODE = 401;
    public static final Integer FORBIDDEN_CODE = 403;

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
        return statusCode == SUCCESS_CODE || statusCode == SUCCESS_NOCONTENT_CODE;
    }

    public boolean isForbidden() {
        return statusCode == FORBIDDEN_CODE;
    }

    public boolean isUnauthorized() {
        return statusCode == UNAUTHORIZED_CODE;
    }
}
