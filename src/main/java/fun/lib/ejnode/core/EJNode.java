package fun.lib.ejnode.core;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class EJNode{

    private static volatile EJNode s_instance = null;
    public static EJNode get(){
        if(s_instance == null){
            synchronized ("EJNode_instance"){
                if(s_instance == null){
                    s_instance = new EJNode();
                }
            }
        }
        return s_instance;
    }

    private EJNode(){
        _initDefaultParams();
    }

    private volatile Class<? extends NodeEntry> _clzEntry;
    public EJNode entry(Class<? extends NodeEntry> clzEntry){
        _clzEntry = clzEntry;
        return this;
    }

    private volatile int _logLevel;
    public EJNode logLevel(int logLevel){
        logLevel = Math.max(Logger.LEVEL_TRACE, logLevel);
        logLevel = Math.min(Logger.LEVEL_FATAL, logLevel);
        _logLevel = logLevel;
        return this;
    }

    private EJLoggerCore _logger;
    private void _initLogger(){
        _logger = new EJLoggerCore(_logLevel);
    }
    private void _startLogger(){
        _logger.start();
    }
    protected EJLoggerCore getLogger(){
        return _logger;
    }

    private volatile int _ioGrpBossNum = 1;
    private volatile int _ioGrpWorkerOutterNum = 1;
    private volatile int _ioGrpWorkerInnerNum = 1;
    private final AtomicBoolean _hasStarted = new AtomicBoolean(false);
    public void start(){
        do{
            if(_hasStarted.getAndSet(true)){
                break;
            }
            _initParams();
            _initIoGroups();
            _initLogger();
            _initTimer();
            _initWorkerMgr();
            ExecutorService thStart = Executors.newFixedThreadPool(1, r -> {
                Thread th = new Thread(r);
                th.setName("EJNode-launch");
                return th;
            });
            _loopLoggerStarted = false;
            _loopTimerStarted = false;
            thStart.submit(()->{
                _startLogger();
                _startTimer();
                while (!_loopLoggerStarted || !_loopTimerStarted){
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                _startWorkerMgr();
            });
            thStart.shutdown();
        }while (false);
    }

    private EJIoMgr _ioMgr;
    private void _initIoGroups(){
        if(_ioMgr == null){
            _ioMgr = new EJIoMgr();
            _ioMgr.start(_ioGrpBossNum, _ioGrpWorkerOutterNum, _ioGrpWorkerInnerNum);
        }
    }
    protected EJIoMgr getIoMgr(){
        return _ioMgr;
    }

    private EJTimerCore _timer;
    private void _initTimer(){
        _timer = new EJTimerCore(EJConst.TIMER_TICK_MS, EJConst.TIMER_WHEEL_SIZE, EJConst.TIMER_WAIT_MS);
    }
    private void _startTimer(){
        _timer.start();
    }
    protected EJTimerCore getTimer(){
        return _timer;
    }

    private EJWorkerMgr _workerMgr;
    private void _initWorkerMgr(){
        _workerMgr = new EJWorkerMgr(_clzEntry);
    }
    private void _startWorkerMgr(){
        _workerMgr.start();
    }
    protected EJWorkerMgr getWorkerMgr(){
        return _workerMgr;
    }


    private void _initParams(){
        _ioGrpBossNum = Math.max(_ioGrpBossNum, 1);
        _ioGrpWorkerOutterNum = Math.max(_ioGrpWorkerOutterNum, 1);
        _ioGrpWorkerInnerNum = Math.max(_ioGrpWorkerInnerNum, 1);
    }
    private void _initDefaultParams(){
        int cpuCoreNum = EJEnvWrap.getCpuCoreNum();
        _ioGrpBossNum = 1;
        _ioGrpWorkerOutterNum = Math.min(cpuCoreNum * 2, EJConst.IO_WORKER_THREAD_MAX);
        _ioGrpWorkerInnerNum = 1;
        //
        _logLevel = Logger.LEVEL_INFO;
    }

    private volatile boolean _loopTimerStarted = false;
    private volatile boolean _loopLoggerStarted = false;

    private final AtomicBoolean _hasShutdown = new AtomicBoolean(false);
    protected void shutdown(){
        if(!_hasShutdown.getAndSet(true)){  // shutdown all workers
            _workerMgr.shutdown();
        }
    }

    private final AtomicInteger _loopWorkerCnt = new AtomicInteger(0);
    protected void onLoopWorkerStart(){
        _loopWorkerCnt.incrementAndGet();
    }
    protected void onLoopWorkerExit(){
        int leftNum = _loopWorkerCnt.decrementAndGet();
        if(leftNum == 0){   // all loop worker exited, shutdown other loops
            int workerNum = _workerMgr.workerNum();  // check accurately by lock
            if(workerNum == 0){
                _ioMgr.shutdown();
                _timer.shutdown();
                _logger.shutdown();
            }
        }
    }

    private final AtomicInteger _loopInnerCnt = new AtomicInteger(0);
    protected void onLoopInnerStart(int type){
        _loopInnerCnt.incrementAndGet();
        if(type == LOOP_TYPE_TIMER){
            _loopTimerStarted = true;
        }else if(type == LOOP_TYPE_LOGGER){
            _loopLoggerStarted = true;
        }
    }
    protected void onLoopInnerExit(int type){
        int leftNum = _loopInnerCnt.decrementAndGet();
        if(leftNum == 0) {   // all loop worker exited, release resource
            _timer.onNodeExit();
            _workerMgr.onNodeExit();
            _ioMgr.onNodeExit();
        }
    }

    protected static final int LOOP_TYPE_TIMER = 1;
    protected static final int LOOP_TYPE_LOGGER = 2;
}


