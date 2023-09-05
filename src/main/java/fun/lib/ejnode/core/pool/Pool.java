package fun.lib.ejnode.core.pool;

public interface Pool<T> {
    T borrow();

    Pool<T> book(BookResult<T> cb);

    String lastError();

    long lastErrorTime();

    void shutdown();

}
