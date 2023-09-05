package fun.lib.ejnode.core.pool;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.Timer;

import java.util.*;

public class ClientPoolCore {

    protected final Net net;
    protected final Timer timer;
    protected final Logger log;
    //
    private String _host;
    private int _port;
    //
    private int _connTimeout;
    private int _poolSize;
    private long _keepAliveInterval;

    private boolean _startCalled;
    private long _clientIdCnt;
    private boolean _shutdownCalled;
    private int _borrowCnt;
    private int _preparingCnt;

    private int _connFailedCnt;
    protected long tmLastError;
    protected String lastError;

    private Map<Long, PoolClient> _mapCliPrepare;
    private LinkedList<PoolClient> _lsCliReady;
    private LinkedList<BookResult<PoolClient>> _lsBookCb;
    private Random _rand;
    private PoolClient.ClientCreator _cbNewClient;

    protected ClientPoolCore(Net net, Timer timer, Logger log){
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
        _keepAliveInterval = 1000 * 30;
    }

    protected void doStart(PoolClient.ClientCreator cbNewClient) {
        if(_startCalled){
            return ;
        }
        _startCalled = true;
        //
        _cbNewClient = cbNewClient;
        _mapCliPrepare = new HashMap<>();
        _lsCliReady = new LinkedList<>();
        _addClient(_poolSize);
    }
    private void _addClient(int addNum){
        _preparingCnt += addNum;
        for(int i=0; i<addNum; ++i){
            PoolClient cli = _cbNewClient.create(++_clientIdCnt, this);
            cli.connect(_connTimeout);
            _mapCliPrepare.put(cli.id, cli);
        }
//        log.debug("try to connect redis server at "+_host+":"+_port);
    }
    public Net net(){
        return net;
    }
    public Timer timer(){
        return timer;
    }
    public long keepAliveInterval(){
        return _keepAliveInterval;
    }
    public void onClientConnDone(PoolClient cli, String error){
        PoolClient cliExist = _mapCliPrepare.remove(cli.id);
        assert cliExist!=null;
        --_preparingCnt;
        assert _preparingCnt >= 0;
        if(_shutdownCalled){
            cliExist.close();
            return;
        }
        if(error == null){  // succ
            _connFailedCnt = 0;
            _lsCliReady.offer(cliExist);
        }else {
            ++_connFailedCnt;
            lastError = error;
            tmLastError = System.currentTimeMillis();
        }
        _checkPool();
    }
    public void onClientDisconn(PoolClient cli){
        PoolClient cliExist = _mapCliPrepare.remove(cli.id);
        if(cliExist != null){   // closed before connDone
            --_preparingCnt;
            assert _preparingCnt >= 0;
            //
            ++_connFailedCnt;
            lastError = "closed by server with no reason";
            tmLastError = System.currentTimeMillis();
        }else{
            Iterator<PoolClient> it = _lsCliReady.iterator();
            while (it.hasNext()){
                if(it.next().id == cli.id){
                    it.remove();
                    break;
                }
            }
        }
        _checkPool();
    }

    public void onClientRelease(PoolClient cli){
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

    private void _procClientBack(PoolClient cli){
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
                PoolClient cli = doBorrow();
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

    protected PoolClient doBorrow() {
        if(_shutdownCalled){
            return null;
        }
        PoolClient cli = _lsCliReady.poll();
        if(cli != null){
            ++_borrowCnt;
            return cli;
        }
        return null;
    }

    protected void doBook(BookResult<PoolClient> cb) {
        if(_shutdownCalled){
            return ;
        }
        if(cb == null){
            return ;
        }
        if(_lsBookCb == null){
            _lsBookCb = new LinkedList<>();
        }
        _lsBookCb.offer(cb);
        _checkPool();
    }

    protected void doShutdown() {
        if(_shutdownCalled){
            return;
        }
        _shutdownCalled = true;
        Iterator<PoolClient> it = _lsCliReady.iterator();
        while (it.hasNext()){
            it.next().close();
            it.remove();
        }
    }

    //
    protected void setPoolSize(int size) {
        _poolSize = Math.max(1, size);
    }
    protected void setConnTimeout(int timeoutMs) {
        _connTimeout = Math.max(0, timeoutMs);
    }
    protected void setKeepAliveInterval(long intervalMs) {
        _keepAliveInterval = Math.max(1000, intervalMs);
    }

    protected void setHost(String host){
        _host = host;
    }
    public String getHost(){
        return _host;
    }
    protected void setPort(int port){
        _port = port;
    }
    public int getPort(){
        return _port;
    }
}
