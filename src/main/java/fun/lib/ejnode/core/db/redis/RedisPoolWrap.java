package fun.lib.ejnode.core.db.redis;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.pool.BookResult;
import fun.lib.ejnode.core.pool.PoolClient;
import fun.lib.ejnode.core.pool.Pool;
import fun.lib.ejnode.core.pool.ClientPoolCore;

public class RedisPoolWrap extends ClientPoolCore implements Pool<RedisClient>, RedisPoolBuilder{

    private String _pwd;

    public RedisPoolWrap(Net net, Timer timer, Logger log){
        super(net, timer, log);
    }

    private final PoolClient.ClientCreator _cbNewClient = new PoolClient.ClientCreator() {
        @Override
        public PoolClient create(long id, ClientPoolCore pool) {
            return new RedisClientWrap(id, pool, _pwd);
        }
    };
    @Override
    public Pool<RedisClient> start() {
        doStart(_cbNewClient);
        return this;
    }
    @Override
    public RedisClient borrow() {
        PoolClient cli = doBorrow();
        if(cli != null){
            return ((RedisClientWrap)cli).getApi();
        }
        return null;
    }
    @Override
    public Pool<RedisClient> book(BookResult<RedisClient> cb) {
        if(cb != null){
            doBook(((error, client) -> {
                if(client != null){
                    cb.onResult(error, ((RedisClientWrap)client).getApi());
                }else{
                    cb.onResult(error, null);
                }
            }));
        }
        return this;
    }
    @Override
    public void shutdown() {
        doShutdown();
    }
    @Override
    public String lastError() {
        return lastError;
    }

    @Override
    public long lastErrorTime() {
        return tmLastError;
    }

    //
    @Override
    public RedisPoolBuilder poolSize(int size) {
        setPoolSize(size);
        return this;
    }
    @Override
    public RedisPoolBuilder connTimeout(int timeoutMs) {
        setConnTimeout(timeoutMs);
        return this;
    }
    @Override
    public RedisPoolBuilder keepAliveInterval(long intervalMs) {
        setKeepAliveInterval(intervalMs);
        return this;
    }

    @Override
    public RedisPoolBuilder connConfig(String host, int port, String password) {
        setHost(host);
        setPort(port);
        _pwd = password;
        return this;
    }

}
