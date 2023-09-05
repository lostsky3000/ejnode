package fun.lib.ejnode.core.db.mysql;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.db.redis.RedisClientWrap;
import fun.lib.ejnode.core.pool.BookResult;
import fun.lib.ejnode.core.pool.ClientPoolCore;
import fun.lib.ejnode.core.pool.Pool;
import fun.lib.ejnode.core.pool.PoolClient;

public class MysqlPoolWrap extends ClientPoolCore implements Pool<MysqlClient>, MysqlPoolBuilder {

    public MysqlPoolWrap(Net net, Timer timer, Logger log) {
        super(net, timer, log);
    }

    private final PoolClient.ClientCreator _cbNewClient = new PoolClient.ClientCreator() {
        @Override
        public PoolClient create(long id, ClientPoolCore pool) {
            return new MysqlClientWrap(id, pool);
        }
    };

    @Override
    public Pool<MysqlClient> start() {
        doStart(_cbNewClient);
        return this;
    }

    //
    @Override
    public MysqlClient borrow() {
        PoolClient cli = doBorrow();
        if(cli != null){
//            return ((RedisClientWrap)cli).getApi();
        }
        return null;
    }

    @Override
    public Pool<MysqlClient> book(BookResult<MysqlClient> cb) {
        if(cb != null){
            doBook(((error, client) -> {
                if(client != null){
//                    cb.onResult(error, ((RedisClientWrap)client).getApi());
                }else{
                    cb.onResult(error, null);
                }
            }));
        }
        return this;
    }

    @Override
    public String lastError() {
        return lastError;
    }
    @Override
    public long lastErrorTime() {
        return tmLastError;
    }
    @Override
    public void shutdown() {
        doShutdown();
    }

    //
    @Override
    public MysqlPoolBuilder poolSize(int size) {
        setPoolSize(size);
        return this;
    }
    @Override
    public MysqlPoolBuilder connTimeout(int timeoutMs) {
        setConnTimeout(timeoutMs);
        return this;
    }
    @Override
    public MysqlPoolBuilder keepAliveInterval(long intervalMs) {
        setKeepAliveInterval(intervalMs);
        return this;
    }

    @Override
    public MysqlPoolBuilder connConfig(String host, int port, String password) {
        setHost(host);
        setPort(port);
        return this;
    }


}
