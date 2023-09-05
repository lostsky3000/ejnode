package fun.lib.ejnode.core.pool;

public interface BookResult<T> {
    void onResult(String error, T client);
}
