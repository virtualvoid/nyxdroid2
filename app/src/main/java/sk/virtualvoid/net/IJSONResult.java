package sk.virtualvoid.net;

public interface IJSONResult {
    boolean isSuccess();

    Error getError();
}
