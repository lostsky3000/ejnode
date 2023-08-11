package fun.lib.ejnode.core.db.redis;

import fun.lib.ejnode.api.callback.CommonFuture;

public interface CommandFuture<T> extends CommonFuture {
    T getResult();
    void onResult(CommandResult<T> listener);
}
