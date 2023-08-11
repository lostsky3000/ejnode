package fun.lib.ejnode.core.db.redis;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.api.db.RedisHelper;

import java.util.Iterator;
import java.util.LinkedList;

public final class RedisHelperWrap implements RedisHelper {

    private final Net _net;
    private final Timer _timer;
    private final Logger _log;

    private LinkedList<RedisPoolWrap> _lsPool;

    public RedisHelperWrap(Net net, Timer timer, Logger log){
        _net = net;
        _timer = timer;
        _log = log;
    }

    @Override
    public RedisPoolBuilder createPool() {
        RedisPoolWrap pool = new RedisPoolWrap(_net, _timer, _log);
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
        Iterator<RedisPoolWrap> it = _lsPool.iterator();
        while (it.hasNext()){
            it.next().shutdown();
            it.remove();
        }
    }

    public void onFrameEnd(){

    }
}
