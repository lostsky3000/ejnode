package fun.lib.ejnode.core.db.redis;

import java.util.List;

public final class RedisApiList {
    private final RedisClientWrap _core;
    private final long _version;

    protected RedisApiList(RedisClientWrap core, long version){
        _core = core;
        _version = version;
    }

    // api
    public CommandFuture<String> index(String key, int index){
        return _core._doSendCommand(_version, RedisCmd.LINDEX, key, index+"");
    }
    public CommandFuture<Long> insertBefore(String key, String pivot, String value){
        return _core._doSendCommand(_version, RedisCmd.LINSERT, key, "BEFORE", pivot, value);
    }
    public CommandFuture<Long> insertAfter(String key, String pivot, String value){
        return _core._doSendCommand(_version, RedisCmd.LINSERT, key, "AFTER", pivot, value);
    }
    public CommandFuture<Long> len(String key){
        return _core._doSendCommand(_version, RedisCmd.LLEN, key);
    }
    // since 6.2.0
    public CommandFuture<String> move(String src, String dst, boolean fromSrcRight, boolean toDstRight){
        return _core._doSendCommand(_version, RedisCmd.LMOVE, src, dst, fromSrcRight?"RIGHT":"LEFT", toDstRight?"RIGHT":"LEFT");
    }
    public CommandFuture<String> pop(String key){
        return _core._doSendCommand(_version, RedisCmd.LPOP, key);
    }
    public CommandFuture<List<Object>> pop(String key, int count){
        return _core._doSendCommand(_version, RedisCmd.LPOP, key, count+"");
    }
    public CommandFuture<Long> push(String key, String...elements){
        return _core._doSendCommand(_version, RedisCmd.LPUSH, key, elements);
    }
    public CommandFuture<Long> pushX(String key, String...elements){
        return _core._doSendCommand(_version, RedisCmd.LPUSHX, key, elements);
    }
    public CommandFuture<List<Object>> range(String key, int start, int stop){
        return _core._doSendCommand(_version, RedisCmd.LRANGE, key, start+"", stop+"");
    }
    public CommandFuture<Long> rem(String key, int count, String element){
        return _core._doSendCommand(_version, RedisCmd.LREM, key, count+"", element);
    }
    public CommandFuture<String> set(String key, int index, String element){
        return _core._doSendCommand(_version, RedisCmd.LSET, key, index+"", element);
    }
    public CommandFuture<String> trim(String key, int start, int stop){
        return _core._doSendCommand(_version, RedisCmd.LTRIM, key, start+"", stop+"");
    }
    public CommandFuture<String> rpop(String key){
        return _core._doSendCommand(_version, RedisCmd.RPOP, key);
    }
    public CommandFuture<List<Object>> rpop(String key, int count){
        return _core._doSendCommand(_version, RedisCmd.RPOP, key, count+"");
    }
    // deprecated since 6.2.0
    public CommandFuture<String> rpoplpush(String src, String dst){
        return _core._doSendCommand(_version, RedisCmd.RPOPLPUSH, src, dst);
    }
    public CommandFuture<Long> rpush(String key, String...elements){
        return _core._doSendCommand(_version, RedisCmd.RPUSH, key, elements);
    }
    public CommandFuture<Long> rpushx(String key, String...elements){
        return _core._doSendCommand(_version, RedisCmd.RPUSHX, key, elements);
    }

    public CommandFuture<List<Object>> brpop(String...params){
        return _core._doSendCommand(_version, RedisCmd.BRPOP, null, params);
    }

    // LMPOP, LPOS
}
