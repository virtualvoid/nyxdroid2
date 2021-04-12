package sk.virtualvoid.nyxdroid.v2.data;

/**
 * @author Juraj
 */
public class NullResponse extends BaseResponse {
    private static final NullResponse success = new NullResponse(true);
    private static final NullResponse failure = new NullResponse(false);

    private NullResponse(boolean success) {
        this.Success = success;
    }

    public static NullResponse success() {
        return success;
    }

    public static NullResponse failure() {
        return failure;
    }
}
