package fun.lib.ejnode.core;

import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.TimerFuture;
import fun.lib.ejnode.api.callback.CbCommon;
import fun.lib.ejnode.api.callback.CbCommonUD;
import fun.lib.ejnode.util.container.DFObjPool;
import fun.lib.ejnode.util.container.DFPooledLinkedList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class LoopWorker implements Runnable {

    private final Class<? extends NodeEntry> _clzEntry;
    public final long pid;
    private final ReentrantLock _lockAwake = new ReentrantLock();
    private final Condition _condAwake;
    private final AtomicBoolean _hasShutdown = new AtomicBoolean(false);

    private boolean _onLoop;
    private final ConcurrentLinkedQueue<MsgPriority> _queMsgPriority = new ConcurrentLinkedQueue<MsgPriority>();
    private final ConcurrentLinkedQueue<MsgIO> _queMsgIO = new ConcurrentLinkedQueue<MsgIO>();
    private final AtomicInteger _waitMsgNum = new AtomicInteger(0);

    private final DFPooledLinkedList<CbWrap<CbCommon, CbCommonUD>> _lsNextTickA = new DFPooledLinkedList<>();
    private final DFPooledLinkedList<CbWrap<CbCommon, CbCommonUD>> _lsNextTickB = new DFPooledLinkedList<>();
    private DFPooledLinkedList<CbWrap<CbCommon, CbCommonUD>> _lsNextTickRead;
    private DFPooledLinkedList<CbWrap<CbCommon, CbCommonUD>> _lsNextTickWrite;

    private final DFPooledLinkedList<CbWrap<CbCommon, CbCommonUD>> _lsImmediate = new DFPooledLinkedList<>();

    private final DFPooledLinkedList<CbWrap<CbCommon, CbCommonUD>> _lsCbWrapPool = new DFPooledLinkedList<>();

    private final DFObjPool<MsgPriority> _msgPriorityPool;
    private final DFObjPool<MsgIO> _msgIoPool;

    private EJEntryWrap _entryWrap;
    private EJTimerWrap _timerWrap;

    private boolean _exitCalled;

    private Object _userData;

    private final ReentrantReadWriteLock _resLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock _resReadLock = _resLock.readLock();
    private final ReentrantReadWriteLock.WriteLock _resWriteLock = _resLock.writeLock();
    private boolean _resReleased;

    protected final ForkParams forkParams;

    protected LoopWorker(Class<? extends NodeEntry> clzEntry, ForkParams forkParams, long pid){
        _clzEntry = clzEntry;
        this.forkParams = forkParams;
        _userData = forkParams.userData();
        this.pid = pid;
        _condAwake = _lockAwake.newCondition();

        _msgPriorityPool = new DFObjPool<>(10000);
        _msgIoPool = new DFObjPool<>(10000);

        _onLoop = true;
        _initResReleasedStatus();
    }

    public void run() {
        s_thlWorkerId.set(this);

        EJNode ejNode = EJNode.get();
        EJWorkerMgr workerMgr = ejNode.getWorkerMgr();
        workerMgr.onWorkerLoopStart(pid);

        _lsNextTickRead = _lsNextTickA;
        _lsNextTickWrite = _lsNextTickB;
        _entryWrap = null;
        boolean shutdownCalled = false;
        //
        do{
            _initEntryWrap();
            if(_entryWrap == null){   // init entry failed
                _userData = null;
                break;
            }
            _exitCalled = false;
            //
            NodeContext.s_thNodeCtx.set(_entryWrap.nodeCtxWrap);
            // call entry.onStart()
            try {
                _entryWrap.entry.onStart(_entryWrap.nodeCtxWrap, _userData);
            }catch (Throwable e){
                e.printStackTrace();
                break;
            }finally {
                _userData = null;
            }
            if(_exitCalled){
                break;
            }
            MsgPriority msgPri = null;
            MsgIO msgIO = null;
            CbWrap<CbCommon, CbCommonUD> cbWrap = null;
            int procMsgCnt = 0;
            int msgType = 0;
            EJNetWrap netWrap = _entryWrap.netWrap;
            _onLoop = true;
            while (_onLoop){
                // proc all nextTick
                _swapLsNextTick();
                while((cbWrap=_lsNextTickRead.poll()) != null){
                    _procCbWrap(cbWrap);
                    if(_exitCalled){
                        break;
                    }
                }
                if(_exitCalled){
                    break;
                }
                // proc immediate work
                cbWrap = _lsImmediate.poll();
                if(cbWrap != null){
                    _procCbWrap(cbWrap);
                }
                if(_exitCalled){
                    break;
                }
                //
                procMsgCnt = 0;
                // proc all priority msg
                while((msgPri = _queMsgPriority.poll()) != null){
                    msgType = msgPri.type;
                    if(msgType == 1){  // timer callback
                        _timerWrap.onTimerCb(msgPri.userData);
                    }else if(msgType == 2){   // msg from another worker by send()
                        _entryWrap.onMsgFromOthers(msgPri.src, msgPri.userData);
                    }else if(msgType == -1){  // shutdown signal
                        shutdownCalled = true;
                        break;
                    }
                    ++procMsgCnt;
                    _recycleMsgPriority(msgPri);
                    if(_exitCalled){
                        break;
                    }
                }
                if(_exitCalled || shutdownCalled){
                    _waitMsgNum.addAndGet(-procMsgCnt);
                    break;
                }
                // proc io msg
                while((msgIO = _queMsgIO.poll()) != null){
                    // proc
                    netWrap.onIoMsg(msgIO);
                    //
                    ++procMsgCnt;
                    _recycleMsgIo(msgIO);
                    if(_exitCalled){
                        break;
                    }
                }
                if(procMsgCnt > 0){
                    _waitMsgNum.addAndGet(-procMsgCnt);
                }
                if(_exitCalled){
                    break;
                }

//                _entryWrap.onLogBatchEnd();
                _entryWrap.onFrameEnd();

                // check nextTick & immediate left num
                if(_lsNextTickWrite.size() <= 0 && _lsImmediate.size() <= 0){
                    // check await
                    _lockAwake.lock();
                    try {
                        int waitMsgNum = _waitMsgNum.get();
                        if(waitMsgNum == 0){
                            //_cond.await(_waitMs, TimeUnit.MILLISECONDS);
                            _condAwake.await();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        _lockAwake.unlock();
                    }
                }
            }
        }while (false);
        // notify entry exitEvent
        if(_entryWrap != null){
            try {
                _entryWrap.entry.onExit(_entryWrap.nodeCtxWrap);
            }catch (Throwable e){
                e.printStackTrace();
            }
            _entryWrap.onLogBatchEnd();
            _entryWrap.onExit();
        }
        // release resources
        _doResRelease();
        //
        NodeContext.s_thNodeCtx.remove();
        // notify workerMgr
        workerMgr.onWorkerLoopExit(pid);
    }

    private void _initResReleasedStatus(){
        _resWriteLock.lock();
        try {
            _resReleased = false;
        }finally {
            _resWriteLock.unlock();
        }
    }
    private void _doResRelease(){
        MsgIO msg;
        EJNetWrap netWrap = null;
        if(_entryWrap != null){
            netWrap = _entryWrap.netWrap;
        }
        int msgCnt = 0;
        _resWriteLock.lock();
        try {
            int msgNum = _waitMsgNum.get();
            while((msg = _queMsgIO.poll()) != null){
                if(!msg.needRelease){
                    continue;
                }
                if(netWrap != null){
                    try {
                        netWrap.onReleaseIoMsg(msg);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
                if(++msgCnt == msgNum){
                    break;
                }
            }
            if(msgCnt > 0){
                _waitMsgNum.addAndGet(-msgCnt);
            }
            _resReleased = true;
        }finally {
            _resWriteLock.unlock();
        }
    }

    protected void callExit(){
        _exitCalled = true;
    }

    private void _initEntryWrap(){
//        Class[] paramsType = {};
//        Object[] params = {};
        NodeEntry entry = null;
        try {
            Constructor<? extends NodeEntry> ctor = _clzEntry.getDeclaredConstructor();
            ctor.setAccessible(true);
            entry = ctor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if(entry != null){
            _entryWrap = new EJEntryWrap(entry, this);
            _timerWrap = _entryWrap.timerWrap;
        }
    }

    private void _recycleMsgPriority(MsgPriority m){
        m.userData = null;
        _msgPriorityPool.recycle(m);
    }
    protected MsgPriority newMsgPriority(){
        MsgPriority msg = _msgPriorityPool.newObj();
        if(msg == null){
            msg = new MsgPriority();
        }
        return msg;
    }
    protected void addPriorityMsg(MsgPriority msg){
        int num = _waitMsgNum.incrementAndGet();
        _queMsgPriority.offer(msg);
        if(num == 1){
            _lockAwake.lock();
            try {
                _condAwake.signal();
            }finally {
                _lockAwake.unlock();
            }
        }
    }

    private void _recycleMsgIo(MsgIO msg){
        msg.userData = null;
        _msgIoPool.recycle(msg);
    }
    public MsgIO newMsgIo(){
        MsgIO msg = _msgIoPool.newObj();
        if(msg == null){
            msg = new MsgIO();
        }
        return msg;
    }
    public boolean addIOMsg(MsgIO msg, boolean msgNeedRelease){
        int num = 0;
        msg.needRelease = msgNeedRelease;
        if(msgNeedRelease){
            _resReadLock.lock();
            try {
                if(!_resReleased){
                    num = _waitMsgNum.incrementAndGet();
                    _queMsgIO.offer(msg);
                }else{   // all res has released, means worker has gone
                    return false;
                }
            }finally {
                _resReadLock.unlock();
            }
        }else{
            num = _waitMsgNum.incrementAndGet();
            _queMsgIO.offer(msg);
        }
        if(num == 1){
            _lockAwake.lock();
            try {
                _condAwake.signal();
            }finally {
                _lockAwake.unlock();
            }
        }
        return true;
    }

    protected void shutdown(){
        if(!_hasShutdown.getAndSet(true)){
            MsgPriority msg = new MsgPriority();
            msg.type = -1;
            addPriorityMsg(msg);
        }
    }

    public EJNetWrap getNetWrap(){
        if(_entryWrap != null){
            return _entryWrap.netWrap;
        }
        return null;
    }
    public NodeContext getNodeCtx(){
        if(_entryWrap != null){
            return _entryWrap.nodeCtxWrap;
        }
        return null;
    }

    /**
     * CbWrap
     */
    static class CbWrap<T,Tud>{
        protected T cb;
        protected Tud cbUd;
        protected Object userData;
        private boolean _canceled;

        protected CbWrapAPI<T,Tud> api;

        protected void reset(T cb, Tud cbUd, Object userData, CbWrapAPI<T,Tud> api){
            this.cb = cb;
            this.cbUd = cbUd;
            this.userData = userData;
            this.api = api;
        }
    }
    /**
     * CbWrapAPI
     */
    static class CbWrapAPI<T,Tud> implements TimerFuture {
        private boolean _canceled;
        private boolean _isDone;
        protected CbWrap<T,Tud> cbWrap;
        protected CbWrapAPI(CbWrap<T,Tud> cbWrap){
            this.cbWrap = cbWrap;
            _canceled = false;
            _isDone = false;
        }
        @Override
        public void cancel() {
            _canceled = true;
        }

        @Override
        public boolean isCanceled() {
            return _canceled;
        }

        @Override
        public boolean isDone() {
            return _isDone;
        }
        protected void setDone(){
            _isDone = true;
        }
    }

    /**
     * next tick
     */
    protected TimerFuture addNextTick(CbCommon cb, CbCommonUD cbUd, Object userData){
        CbWrap<CbCommon, CbCommonUD> wrap = _newCbWrap();

        CbWrapAPI<CbCommon,CbCommonUD> api = new CbWrapAPI<>(wrap);

        wrap.reset(cb, cbUd, userData, api);
        _lsNextTickWrite.offer(wrap);

        return api;
    }
    private void _swapLsNextTick(){
        DFPooledLinkedList<CbWrap<CbCommon, CbCommonUD>> lsTmp = _lsNextTickRead;
        _lsNextTickRead = _lsNextTickWrite;
        _lsNextTickWrite = lsTmp;
    }

    /**
     *  immediate
     */
    protected TimerFuture addImmediate(CbCommon cb, CbCommonUD cbUd, Object userData){
        CbWrap<CbCommon, CbCommonUD> wrap = _newCbWrap();

        CbWrapAPI<CbCommon,CbCommonUD> api = new CbWrapAPI<>(wrap);

        wrap.reset(cb, cbUd, userData, api);
        _lsImmediate.offer(wrap);

        return api;
    }

    private CbWrap<CbCommon,CbCommonUD> _newCbWrap(){
        CbWrap<CbCommon,CbCommonUD> wrap = _lsCbWrapPool.poll();
        if(wrap == null){
            wrap = new CbWrap<>();
        }
        return wrap;
    }
    private void _recycleCbWrap(CbWrap<CbCommon, CbCommonUD> wrap){
        wrap.reset(null, null, null, null);
        _lsCbWrapPool.offer(wrap);
    }

    private void _procCbWrap(CbWrap<CbCommon, CbCommonUD> cbWrap){
        CbWrapAPI<CbCommon,CbCommonUD> api = cbWrap.api;
        if(!api.isCanceled()){
            try{
                if(cbWrap.cb != null){
                    cbWrap.cb.onCallback();
                }else{
                    cbWrap.cbUd.onCallback(cbWrap.userData);
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
            api.setDone();   // mark task has done
        }
        api.cbWrap = null;
        _recycleCbWrap(cbWrap);
    }

    private static ThreadLocal<LoopWorker> s_thlWorkerId = new ThreadLocal<>();
    public static LoopWorker getThlWorker(){
        return s_thlWorkerId.get();
    }
}


