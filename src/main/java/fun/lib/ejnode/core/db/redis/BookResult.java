package fun.lib.ejnode.core.db.redis;

public interface BookResult {
    void onResult(String error, RedisClient client);
}
