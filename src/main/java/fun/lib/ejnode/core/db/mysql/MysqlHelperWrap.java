package fun.lib.ejnode.core.db.mysql;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.api.db.MysqlHelper;
import fun.lib.ejnode.core.db.redis.RedisPoolWrap;

import java.util.Iterator;
import java.util.LinkedList;

public class MysqlHelperWrap implements MysqlHelper {

    private final Net _net;
    private final Timer _timer;
    private final Logger _log;

    private LinkedList<MysqlPoolWrap> _lsPool;

    public MysqlHelperWrap(Net net, Timer timer, Logger log){
        _net = net;
        _timer = timer;
        _log = log;
    }

    @Override
    public MysqlPoolBuilder createPool() {
        MysqlPoolWrap pool = new MysqlPoolWrap(_net, _timer, _log);
        if(_lsPool == null){
            _lsPool = new LinkedList<>();
        }
        _lsPool.offer(pool);
        return pool;
    }

    public void onExit(){
        if(_lsPool == null){
            return;
        }
        Iterator<MysqlPoolWrap> it = _lsPool.iterator();
        while (it.hasNext()){
            it.next().shutdown();
            it.remove();
        }
    }

    public void onFrameEnd(){

    }
}
