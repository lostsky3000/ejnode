package fun.lib.ejnode.core.db.redis;

public interface CommandResult<T> {

    void onResult(String error, T result);

}
