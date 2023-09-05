package fun.lib.ejnode.core.db.mysql;

import fun.lib.ejnode.core.pool.Pool;

public interface MysqlPoolBuilder {

    MysqlPoolBuilder poolSize(int size);

    MysqlPoolBuilder connTimeout(int timeoutMs);

    MysqlPoolBuilder keepAliveInterval(long intervalMs);

    Pool<MysqlClient> start();


    MysqlPoolBuilder connConfig(String host, int port, String password);

}
