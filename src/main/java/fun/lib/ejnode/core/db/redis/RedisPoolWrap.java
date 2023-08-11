package fun.lib.ejnode.core.db.redis;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.Timer;

import java.util.*;

public final class RedisPoolWrap implements RedisPool, RedisPoolBuilder{

    protected final Net net;
    protected final Timer timer;
    protected final Logger log;

    private String _host;
    private int _port;
    private String _pwd;
    private int _connTimeout;
    private int _poolSize;
    protected long keepAliveInterval;

    private boolean _startCalled;
    private long _clientIdCnt;
    private boolean _shutdownCalled;
    private int _borrowCnt;
    private int _preparingCnt;

    private long _tmLastAdd;
    private int _connFailedCnt;
    private long _tmLastError;
    private String _lastError;

    private Map<Long, RedisClientWrap> _mapCliPrepare;
    private LinkedList<RedisClientWrap> _lsCliReady;
    private LinkedList<BookResult> _lsBookCb;
    private Random _rand;

    public RedisPoolWrap(Net net, Timer timer, Logger log){
        this.net = net;
        this.timer = timer;
        this.log = log;
        _initDefault();
        //
        _clientIdCnt = 0;
        _startCalled = false;
        _shutdownCalled = false;
        _borrowCnt = 0;
        _preparingCnt = 0;
        _connFailedCnt = 0;
    }
    private void _initDefault(){
        _connTimeout = 1000 * 10;
        _poolSize = 1;
        keepAliveInterval = 1000 * 30;
    }

    @Override
    public RedisPool start() {
        if(_startCalled){
            return this;
        }
        _startCalled = true;
        //
        _mapCliPrepare = new HashMap<>();
        _lsCliReady = new LinkedList<>();
        _addClient(_poolSize);
        return this;
    }

    private void _addClient(int addNum){
        _preparingCnt += addNum;
        for(int i=0; i<addNum; ++i){
            RedisClientWrap cliWrap = new RedisClientWrap(++_clientIdCnt, this, _host, _port, _pwd);
            cliWrap.connect(_connTimeout);
            _mapCliPrepare.put(cliWrap.id, cliWrap);
        }
        _tmLastAdd = System.currentTimeMillis();
        log.debug("try to connect redis server at "+_host+":"+_port);
    }

    protected void onClientConnDone(RedisClientWrap cli, String error){
        RedisClientWrap cliWrap = _mapCliPrepare.remove(cli.id);
        assert cliWrap!=null;
        --_preparingCnt;
        assert _preparingCnt >= 0;
        if(_shutdownCalled){
            cliWrap.close();
            return;
        }
        if(error == null){  // conn & auth succ
            _connFailedCnt = 0;
            _lsCliReady.offer(cliWrap);
        }else {
            ++_connFailedCnt;
            _lastError = error;
            _tmLastError = System.currentTimeMillis();
        }
        _checkPool();
    }
    protected void onClientDisconn(RedisClientWrap cli){
        RedisClientWrap cliWrap = _mapCliPrepare.remove(cli.id);
        if(cliWrap != null){   // closed before connDone
            --_preparingCnt;
            assert _preparingCnt >= 0;
            //
            ++_connFailedCnt;
            _lastError = "closed by server with no reason";
            _tmLastError = System.currentTimeMillis();
        }else{
            Iterator<RedisClientWrap> it = _lsCliReady.iterator();
            while (it.hasNext()){
                if(it.next().id == cli.id){
                    it.remove();
                    break;
                }
            }
        }
        _checkPool();
    }
    protected void onClientDecodeError(RedisClientWrap cli, String error){

    }

    protected void onClientRelease(RedisClientWrap cli){
        if(_shutdownCalled){
            cli.close();
            return;
        }
        if(cli.isAlive()){
            _procClientBack(cli);
        }
        --_borrowCnt;
        assert _borrowCnt >= 0;
        _checkPool();
    }

    private void _procClientBack(RedisClientWrap cli){
        int actNum = _borrowCnt + _lsCliReady.size();
        if(actNum <= _poolSize){
            _lsCliReady.offer(cli);
        }else {
            cli.close();
        }
    }

    private boolean _checkPoolCalled = false;
    private void _checkPool(){
        if(_shutdownCalled){
            return;
        }
        if(_checkPoolCalled){
            return;
        }
        _checkPoolCalled = true;
        timer.nextTick(this::_doCheckPool);
    }
    private void _doCheckPool(){
        _checkPoolCalled = false;
        if(_shutdownCalled){
            return;
        }
        assert _borrowCnt >= 0;
        assert _preparingCnt >= 0;
        // proc book result
        int bookNum = _procBooking();
        int needAdd4Book = bookNum - _preparingCnt;
        int needAdd4Pool = _poolSize - (_borrowCnt + _lsCliReady.size() + _preparingCnt);
        if(needAdd4Pool > 0 || needAdd4Book > 0){
            long tmDelay = _connFailedCnt * 90;
            if(tmDelay < 100){
                _addClient(1);
            }else{
                tmDelay = Math.min(20000, tmDelay);
                if(tmDelay >= 5000){
                    if(_rand == null){
                        _rand = new Random();
                    }
                    int base = (int) (tmDelay / 2);
                    tmDelay = base + _rand.nextInt((int) (tmDelay - base));
                }
                timer.timeout(tmDelay, ()->{
                    if(_shutdownCalled){
                        return;
                    }
                    int bookNum2 = _procBooking();
                    int add4Book = bookNum2 - _preparingCnt;
                    int add4Pool = _poolSize - (_borrowCnt + _lsCliReady.size() + _preparingCnt);
                    if(add4Book > 0 || add4Pool > 0){
                        _addClient(1);
                    }
                });
            }
        }
    }

    private int _procBooking(){
        int bookNum = 0;
        if(_lsBookCb != null){
            bookNum = _lsBookCb.size();
            while (bookNum > 0){
                RedisClient cli = borrow();
                if(cli != null){
                    --bookNum;
                    try {
                        _lsBookCb.poll().onResult(null, cli);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }else {
                    break;
                }
            }
        }
        return bookNum;
    }


    @Override
    public RedisClient borrow() {
        if(_shutdownCalled){
            return null;
        }
        RedisClientWrap cli = _lsCliReady.poll();
        if(cli != null){
            ++_borrowCnt;
            return cli.getApi();
        }
        return null;
    }

    @Override
    public RedisPool book(BookResult cb) {
        if(_shutdownCalled){
            return null;
        }
        if(cb == null){
            return this;
        }
        if(_lsBookCb == null){
            _lsBookCb = new LinkedList<>();
        }
        _lsBookCb.offer(cb);
        _checkPool();
        return this;
    }

    @Override
    public String lastError() {
        return _lastError;
    }

    @Override
    public long lastErrorTime() {
        return _tmLastError;
    }

    @Override
    public void shutdown() {
        if(_shutdownCalled){
            return;
        }
        _shutdownCalled = true;
        Iterator<RedisClientWrap> it = _lsCliReady.iterator();
        while (it.hasNext()){
            it.next().close();
            it.remove();
        }
    }

    //
    @Override
    public RedisPoolBuilder poolSize(int size) {
        _poolSize = Math.max(1, size);
        return this;
    }

    @Override
    public RedisPoolBuilder connTimeout(int timeoutMs) {
        _connTimeout = Math.max(0, timeoutMs);
        return this;
    }

    @Override
    public RedisPoolBuilder connConfig(String host, int port, String password) {
        _host = host;
        _port = port;
        _pwd = password;
        return this;
    }

    @Override
    public RedisPoolBuilder keepAliveInterval(long intervalMs) {
        keepAliveInterval = Math.max(1000, intervalMs);
        return this;
    }
}
