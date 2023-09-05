package fun.lib.ejnode.api.db;


import fun.lib.ejnode.core.db.mysql.MysqlPoolBuilder;

public interface MysqlHelper {

    MysqlPoolBuilder createPool();
}
