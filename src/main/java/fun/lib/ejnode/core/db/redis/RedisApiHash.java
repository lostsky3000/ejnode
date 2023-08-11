package fun.lib.ejnode.core.db.redis;

import java.util.List;

public final class RedisApiHash {

    private final RedisClientWrap _core;
    private final long _version;

    protected RedisApiHash(RedisClientWrap core, long version){
        _core = core;
        _version = version;
    }

    // api
    public CommandFuture<Long> del(String key, String... fields) {
        return _core._doSendCommand(_version, RedisCmd.HDEL, key, fields);
    }
    public CommandFuture<Long> exists(String key, String field) {
        return _core._doSendCommand(_version, RedisCmd.HEXISTS, key, field);
    }
    public CommandFuture<String> get(String key, String field) {
        return _core._doSendCommand(_version, RedisCmd.HGET, key, field);
    }
    public CommandFuture<List<Object>> getAll(String key) {
        return _core._doSendCommand(_version, RedisCmd.HGETALL, key);
    }
    public CommandFuture<Long> incrBy(String key, String field, int increment) {
        return _core._doSendCommand(_version, RedisCmd.HINCRBY, key, field, increment+"");
    }
    public CommandFuture<String> incrByFloat(String key, String field, float increment) {
        return _core._doSendCommand(_version, RedisCmd.HINCRBYFLOAT, key, field, increment+"");
    }
    public CommandFuture<List<Object>> keys(String key) {
        return _core._doSendCommand(_version, RedisCmd.HKEYS, key);
    }
    public CommandFuture<Long> len(String key) {
        return _core._doSendCommand(_version, RedisCmd.HLEN, key);
    }
    public CommandFuture<List<Object>> mGet(String key, String... fields) {
        return _core._doSendCommand(_version, RedisCmd.HMGET, key, fields);
    }
    public CommandFuture<String> mSet(String key, String... pairs) {
        return _core._doSendCommand(_version, RedisCmd.HMSET, key, pairs);
    }
    public CommandFuture<String> randField(String key) {
        return _core._doSendCommand(_version, RedisCmd.HRANDFIELD, key);
    }
    public CommandFuture<List<Object>> randField(String key, int count, boolean withValues) {
        if(withValues){
            return _core._doSendCommand(_version, RedisCmd.HRANDFIELD, key, count+"", "WITHVALUES");
        }
        return _core._doSendCommand(_version, RedisCmd.HRANDFIELD, key, count+"");
    }
    public CommandFuture<Long> set(String key, String field, String value) {
        return _core._doSendCommand(_version, RedisCmd.HSET, key, field, value);
    }
    public CommandFuture<Long> set(String key, String... pairs) {
        return _core._doSendCommand(_version, RedisCmd.HSET, key, pairs);
    }
    public CommandFuture<Long> setNX(String key, String field, String value) {
        return _core._doSendCommand(_version, RedisCmd.HSETNX, key, field, value);
    }
    public CommandFuture<Long> strLen(String key, String field) {
        return _core._doSendCommand(_version, RedisCmd.HSTRLEN, key, field);
    }
    public CommandFuture<List<Object>> vals(String key) {
        return _core._doSendCommand(_version, RedisCmd.HVALS, key);
    }

}
