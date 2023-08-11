package fun.lib.ejnode.api;

public interface TimerFuture {

    void cancel();

    boolean isCanceled();

    boolean isDone();
}
