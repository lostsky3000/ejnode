package fun.lib.ejnode.core.db.redis;

public interface RedisPool {

    RedisClient borrow();

    RedisPool book(BookResult cb);

    String lastError();

    long lastErrorTime();

    void shutdown();

}
