package fun.lib.ejnode.api.callback;

public interface CommonFuture {

    boolean isDone();
    boolean isSucc();
    String error();
}
