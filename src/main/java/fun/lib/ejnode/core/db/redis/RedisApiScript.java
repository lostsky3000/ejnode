package fun.lib.ejnode.core.db.redis;

import java.util.List;

public final class RedisApiScript {
    private final RedisClientWrap _core;
    private final long _version;

    protected RedisApiScript(RedisClientWrap core, long version){
        _core = core;
        _version = version;
    }

    // api
    public CommandFuture<String> scriptLoad(String script){
        return _core._doSendCommand(_version, RedisCmd.SCRIPT, "LOAD", script);
    }
    public CommandFuture<List<Object>> scriptExists(String... sha1s){
        return _core._doSendCommand(_version, RedisCmd.SCRIPT, "EXISTS", sha1s);
    }
    public CommandFuture<String> scriptKill(){
        return _core._doSendCommand(_version, RedisCmd.SCRIPT, "KILL");
    }

    public CommandFuture<Object> eval(String script, String... args){
        return _core._doSendCommand(_version, RedisCmd.EVAL, script, args);
    }
    public CommandFuture<Object> evalSHA(String sha1, String... args){
        return _core._doSendCommand(_version, RedisCmd.EVALSHA, sha1, args);
    }
}
