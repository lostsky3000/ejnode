package fun.lib.ejnode.core.db.redis;

import java.util.List;

public interface RedisClient {

//    CommandFuture<String> auth(String password);

    CommandFuture<String> get(String key);

    CommandFuture<String> set(String key, String value);

    CommandFuture<Long> del(String... keys);

    CommandFuture<Object> ping();
    CommandFuture<String> info();

    CommandFuture<Long> decr(String key);
    CommandFuture<Long> decrBy(String key, long decrement);

    CommandFuture<Long> incr(String key);
    CommandFuture<Long> incrBy(String key, long increment);
    CommandFuture<String> incrByFloat(String key, float increment);

    CommandFuture<String> echo(String msg);

    CommandFuture<Long> exists(String... keys);

    CommandFuture<Long> expire(String key, long seconds);
    CommandFuture<Long> expireAt(String key, long timestamp);
    CommandFuture<Long> expireTime(String key);  // since 7.0.0

    CommandFuture<String> getDel(String key);  //since 6.2.0

    CommandFuture<String> getRange(String key, int start, int end);

    CommandFuture<String> getSet(String key, String value);  //deprecated since 6.2.0

    CommandFuture<List<Object>> mGet(String...keys);

    CommandFuture<String> mSet(String...pairs);
    CommandFuture<Long> mSetNX(String...pairs);

    CommandFuture<String> randomKey();
    CommandFuture<String> rename(String key, String newKey);
    CommandFuture<Long> renameNX(String key, String newKey);
    CommandFuture<Long> strLen(String key);
    CommandFuture<List<Object>> time();
    CommandFuture<Long> touch(String... keys);
    CommandFuture<Long> ttl(String key);
    CommandFuture<String> type(String key);
    CommandFuture<Long> unlink(String... keys);
    CommandFuture<Long> append(String key, String value);
    // since 6.2.0
    CommandFuture<String> getEX(String key, String...args);

    CommandFuture<Long> setRange(String key, int offset, String value);

    CommandFuture<String> select(int index);

    SubsFuture subscribe(String channel, String... channels);
    UnsubsFuture unsubscribe(String... channels);
    SubsFuture psubscribe(String... channelPatterns);
    CommandFuture<Long> publish(String channel, String msg);

    RedisApiHash apiHash();
    RedisApiList apiList();
    RedisApiSet apiSet();
    RedisApiZSet apiZSet();
    RedisApiScript apiScript();

    void release();

}
