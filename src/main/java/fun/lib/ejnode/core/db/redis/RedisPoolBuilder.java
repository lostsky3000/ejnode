package fun.lib.ejnode.core.db.redis;

import fun.lib.ejnode.core.pool.Pool;

public interface RedisPoolBuilder {

    RedisPoolBuilder poolSize(int size);

    RedisPoolBuilder connTimeout(int timeoutMs);

    RedisPoolBuilder connConfig(String host, int port, String password);

    RedisPoolBuilder keepAliveInterval(long intervalMs);

    Pool<RedisClient> start();

}
