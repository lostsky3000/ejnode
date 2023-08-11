package fun.lib.ejnode.core.db.redis;

import java.util.List;

public final class RedisApiSet {
    private final RedisClientWrap _core;
    private final long _version;

    protected RedisApiSet(RedisClientWrap core, long version){
        _core = core;
        _version = version;
    }

    // api
    public CommandFuture<Long> add(String key, String... members){
        return _core._doSendCommand(_version, RedisCmd.SADD, key, members);
    }
    public CommandFuture<Long> card(String key){
        return _core._doSendCommand(_version, RedisCmd.SCARD, key);
    }
    public CommandFuture<List<Object>> diff(String... keys){
        return _core._doSendCommand(_version, RedisCmd.SDIFF, null, keys);
    }
    public CommandFuture<Long> diffStore(String dst, String... keys){
        return _core._doSendCommand(_version, RedisCmd.SDIFFSTORE, dst, keys);
    }
    public CommandFuture<List<Object>> inter(String... keys){
        return _core._doSendCommand(_version, RedisCmd.SINTER, null, keys);
    }
    public CommandFuture<Long> interStore(String dst, String... keys){
        return _core._doSendCommand(_version, RedisCmd.SINTERSTORE, dst, keys);
    }
    // since 7.0.0
    public CommandFuture<Long> interCard(String...keys){
        return _core._doSendCommand(_version, RedisCmd.SINTERCARD, keys.length+"", keys);
    }
    public CommandFuture<Long> isMember(String key, String member){
        return _core._doSendCommand(_version, RedisCmd.SISMEMBER, key, member);
    }
    public CommandFuture<List<Object>> members(String key){
        return _core._doSendCommand(_version, RedisCmd.SMEMBERS, key);
    }
    // since 6.2.0
    public CommandFuture<List<Object>> mIsMember(String key, String... members){
        return _core._doSendCommand(_version, RedisCmd.SMISMEMBER, key, members);
    }
    public CommandFuture<Long> move(String src, String dst, String member){
        return _core._doSendCommand(_version, RedisCmd.SMOVE, src, dst, member);
    }
    public CommandFuture<String> pop(String key){
        return _core._doSendCommand(_version, RedisCmd.SPOP, key);
    }
    public CommandFuture<List<Object>> pop(String key, int count){
        return _core._doSendCommand(_version, RedisCmd.SPOP, key, count+"");
    }
    public CommandFuture<String> randMember(String key){
        return _core._doSendCommand(_version, RedisCmd.SRANDMEMBER, key);
    }
    public CommandFuture<List<Object>> randMember(String key, int count){
        return _core._doSendCommand(_version, RedisCmd.SRANDMEMBER, key, count+"");
    }
    public CommandFuture<Long> rem(String key, String... members){
        return _core._doSendCommand(_version, RedisCmd.SREM, key, members);
    }
    public CommandFuture<List<Object>> union(String... keys){
        return _core._doSendCommand(_version, RedisCmd.SUNION, null, keys);
    }
    public CommandFuture<Long> unionStore(String dst, String... keys){
        return _core._doSendCommand(_version, RedisCmd.SUNIONSTORE, dst, keys);
    }

    // SSCAN
}
