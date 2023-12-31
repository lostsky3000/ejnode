package fun.lib.ejnode.core;

import fun.lib.ejnode.api.Db;
import fun.lib.ejnode.api.db.MysqlHelper;
import fun.lib.ejnode.api.db.RedisHelper;
import fun.lib.ejnode.core.db.mysql.MysqlHelperWrap;
import fun.lib.ejnode.core.db.redis.RedisHelperWrap;

public final class EJDbWrap implements Db {

    private RedisHelperWrap _redisHelperWrap;
    private MysqlHelperWrap _mysqlHelperWrap;

    private final EJNetWrap _netWrap;
    private final EJTimerWrap _timerWrap;
    private final EJLoggerWrap _logWrap;

    protected EJDbWrap(EJNetWrap netWrap, EJTimerWrap timerWrap, EJLoggerWrap logWrap){
        _netWrap = netWrap;
        _timerWrap = timerWrap;
        _logWrap = logWrap;
    }

    @Override
    public RedisHelper redis() {
        if(_redisHelperWrap == null){
            _redisHelperWrap = new RedisHelperWrap(_netWrap, _timerWrap, _logWrap);
        }
        return _redisHelperWrap;
    }

//    @Override
//    public MysqlHelper mysql() {
//        if(_mysqlHelperWrap == null){
//            _mysqlHelperWrap = new MysqlHelperWrap(_netWrap, _timerWrap, _logWrap);
//        }
//        return _mysqlHelperWrap;
//    }

    protected void onExit(){
        if(_redisHelperWrap != null){
            _redisHelperWrap.onExit();
        }
        if(_mysqlHelperWrap != null){
            _mysqlHelperWrap.onExit();
        }
    }

    protected void onFrameEnd(){
        if(_redisHelperWrap != null){
            _redisHelperWrap.onFrameEnd();
        }
        if(_mysqlHelperWrap != null){
            _mysqlHelperWrap.onFrameEnd();
        }
    }
}
