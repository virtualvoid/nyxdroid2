package sk.virtualvoid.nyxdroid.v2.data;

public class SuccessResponse<TData> {
    private final TData data;
    private final Context context;

    public SuccessResponse(TData data, Context context) {
        this.data = data;
        this.context = context;
    }

    public TData getData() {
        return data;
    }

    public Context getContext() {
        return context;
    }
}
