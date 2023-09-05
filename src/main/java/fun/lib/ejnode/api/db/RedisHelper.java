package fun.lib.ejnode.api.db;

import fun.lib.ejnode.core.db.redis.RedisPoolBuilder;

public interface RedisHelper {

    RedisPoolBuilder createPool();

}
