package fun.lib.ejnode.api;

import fun.lib.ejnode.api.db.MysqlHelper;
import fun.lib.ejnode.api.db.RedisHelper;

public interface Db {

    RedisHelper redis();

//    MysqlHelper mysql();
}
