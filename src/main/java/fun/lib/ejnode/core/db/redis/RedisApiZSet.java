package fun.lib.ejnode.core.db.redis;

import java.util.List;

public final class RedisApiZSet {

    private final RedisClientWrap _core;
    private final long _version;

    protected RedisApiZSet(RedisClientWrap core, long version){
        _core = core;
        _version = version;
    }

    //api
    public CommandFuture<Long> add(String key, String...pairs){
        return _core._doSendCommand(_version, RedisCmd.ZADD, key, pairs);
    }
    public CommandFuture<Long> card(String key){
        return _core._doSendCommand(_version, RedisCmd.ZCARD, key);
    }
    public CommandFuture<Long> count(String key, int min, int max){
        return _core._doSendCommand(_version, RedisCmd.ZCOUNT, key, min+"", max+"");
    }
    // since 6.2.0
    public CommandFuture<List<Object>> diff(String... keys){
        return _core._doSendCommand(_version, RedisCmd.ZDIFF, keys.length+"", keys);
    }
    // since 6.2.0
    public CommandFuture<List<Object>> diffWithScores(String... keys){
        int keyNum = keys.length;
        String[] arr = new String[keyNum + 1];
        System.arraycopy(keys, 0, arr, 0, keyNum);
        arr[keyNum] = "WITHSCORES";
        return _core._doSendCommand(_version, RedisCmd.ZDIFF, keyNum+"", arr);
    }
    // since 6.2.0
    public CommandFuture<Long> diffStore(String dst, String... keys){
        int keyNum = keys.length;
        String[] arr = new String[keyNum + 1];
        arr[0] = keyNum + "";
        System.arraycopy(keys, 0, arr, 1, keyNum);
        return _core._doSendCommand(_version, RedisCmd.ZDIFFSTORE, dst, arr);
    }
    public CommandFuture<String> incrBy(String key, float increment, String member){
        return _core._doSendCommand(_version, RedisCmd.ZINCRBY, key, increment+"", member);
    }
    public CommandFuture<List<Object>> inter(String... keys){
        return _core._doSendCommand(_version, RedisCmd.ZINTER, keys.length+"", keys);
    }
    public CommandFuture<Long> interCard(String... keys){
        return _core._doSendCommand(_version, RedisCmd.ZINTERCARD, keys.length+"", keys);
    }
    public CommandFuture<Long> interStore(String dst, String... keys){
        int keyNum = keys.length;
        String[] arr = new String[keyNum + 1];
        arr[0] = keyNum + "";
        System.arraycopy(keys, 0, arr, 1, keyNum);
        return _core._doSendCommand(_version, RedisCmd.ZINTERSTORE, dst, arr);
    }
    public CommandFuture<Long> lexCount(String key, String min, String max){
        return _core._doSendCommand(_version, RedisCmd.ZLEXCOUNT, key, min, max);
    }
    // since 7.0.0
    public CommandFuture<List<Object>> mPop(boolean isDesc, String... keys){
        int keyNum = keys.length;
        String[] arr = new String[keyNum + 1];
        System.arraycopy(keys, 0, arr, 0, keyNum);
        arr[keyNum] = isDesc?"MAX":"MIN";
        return _core._doSendCommand(_version, RedisCmd.ZMPOP, keyNum+"", arr);
    }
    public CommandFuture<List<Object>> mPop(boolean isDesc, int count, String... keys){
        int keyNum = keys.length;
        String[] arr = new String[keyNum + 3];
        System.arraycopy(keys, 0, arr, 0, keyNum);
        arr[keyNum] = isDesc?"MAX":"MIN";
        arr[keyNum+1] = "COUNT";
        arr[keyNum+2] = count+"";
        return _core._doSendCommand(_version, RedisCmd.ZMPOP, keyNum+"", arr);
    }
    // since 6.2.0
    public CommandFuture<List<Object>> mScore(String key, String... members){
        return _core._doSendCommand(_version, RedisCmd.ZMSCORE, key, members);
    }
    // since 5.0.0
    public CommandFuture<List<Object>> popMax(String key){
        return _core._doSendCommand(_version, RedisCmd.ZPOPMAX, key);
    }
    // since 5.0.0
    public CommandFuture<List<Object>> popMax(String key, int count){
        return _core._doSendCommand(_version, RedisCmd.ZPOPMAX, key, count+"");
    }
    // since 5.0.0
    public CommandFuture<List<Object>> popMin(String key){
        return _core._doSendCommand(_version, RedisCmd.ZPOPMIN, key);
    }
    // since 5.0.0
    public CommandFuture<List<Object>> popMin(String key, int count){
        return _core._doSendCommand(_version, RedisCmd.ZPOPMIN, key, count+"");
    }

    // since 6.2.0
    public CommandFuture<String> randMember(String key){
        return _core._doSendCommand(_version, RedisCmd.ZRANDMEMBER, key);
    }
    // since 6.2.0
    public CommandFuture<List<Object>> randMember(String key, int count, boolean withScores){
        if(withScores){
            return _core._doSendCommand(_version, RedisCmd.ZRANDMEMBER, key, count+"", "WITHSCORES");
        }
        return _core._doSendCommand(_version, RedisCmd.ZRANDMEMBER, key, count+"");
    }
    public CommandFuture<List<Object>> range(String key, String...args){
        return _core._doSendCommand(_version, RedisCmd.ZRANGE, key, args);
    }
    // since 6.2.0
    public CommandFuture<Long> rangeStore(String... args){
        return _core._doSendCommand(_version, RedisCmd.ZRANGESTORE, null, args);
    }
    public CommandFuture<Long> rank(String key, String member){
        return _core._doSendCommand(_version, RedisCmd.ZRANK, key, member);
    }
    // since 7.2.0
    public CommandFuture<List<Object>> rankWithScore(String key, String member){
        return _core._doSendCommand(_version, RedisCmd.ZRANK, key, member, "WITHSCORE");
    }
    public CommandFuture<Long> rem(String key, String... members){
        return _core._doSendCommand(_version, RedisCmd.ZREM, key, members);
    }
    public CommandFuture<Long> remRangeByLex(String key, String... args){
        return _core._doSendCommand(_version, RedisCmd.ZREMRANGEBYLEX, key, args);
    }
    public CommandFuture<Long> remRangeByRank(String key, int start, int stop){
        return _core._doSendCommand(_version, RedisCmd.ZREMRANGEBYRANK, key, start+"", stop+"");
    }
    public CommandFuture<Long> remRangeByScore(String key, float min, float max){
        return _core._doSendCommand(_version, RedisCmd.ZREMRANGEBYSCORE, key, min+"", max+"");
    }
    public CommandFuture<List<Object>> revRange(String key, int start, int stop){
        return _core._doSendCommand(_version, RedisCmd.ZREVRANGE, key, start+"", stop+"");
    }
    public CommandFuture<List<Object>> revRangeWithScores(String key, int start, int stop){
        return _core._doSendCommand(_version, RedisCmd.ZREVRANGE, key, start+"", stop+"", "WITHSCORES");
    }
    public CommandFuture<List<Object>> revRangeByLex(String key, String... args){
        return _core._doSendCommand(_version, RedisCmd.ZREVRANGEBYLEX, key, args);
    }
    public CommandFuture<List<Object>> revRangeByScore(String key, String... args){
        return _core._doSendCommand(_version, RedisCmd.ZREVRANGEBYSCORE, key, args);
    }
    public CommandFuture<Long> revRank(String key, String member){
        return _core._doSendCommand(_version, RedisCmd.ZREVRANK, key, member);
    }
    // since 7.2.0
    public CommandFuture<List<Object>> revRankWithScore(String key, String member){
        return _core._doSendCommand(_version, RedisCmd.ZREVRANK, key, member, "WITHSCORE");
    }
    public CommandFuture<String> score(String key, String member){
        return _core._doSendCommand(_version, RedisCmd.ZSCORE, key, member);
    }
    // since 6.2.0
    public CommandFuture<List<Object>> union(String... args){
        return _core._doSendCommand(_version, RedisCmd.ZUNION, null, args);
    }
    public CommandFuture<Long> unionStore(String... args){
        return _core._doSendCommand(_version, RedisCmd.ZUNIONSTORE, null, args);
    }


    // ZCAN
}
