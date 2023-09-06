package fun.lib.ejnode.core;

import fun.lib.ejnode.api.NodeEntry;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class EJWorkerMgr implements EJNodeLife{

    private final Class<? extends NodeEntry> _clzEntryFirst;
    private final ExecutorService _thPoolWorker;

    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock _writeLock;
    private final ReentrantReadWriteLock.ReadLock _readLock;

    private final EJNode _ejNode;

    private HashMap<Long, WorkerWrap> _mapWorker;
    private long _workerPidCnt;
    private final ConcurrentLinkedQueue<Long> _queForkPid = new ConcurrentLinkedQueue<>();
    private final ForkParams _forkParamsFirst;

    protected EJWorkerMgr(Class<? extends NodeEntry> clzEntryFirst, ForkParams forkParamsFirst){
        _clzEntryFirst = clzEntryFirst;
        _forkParamsFirst = forkParamsFirst;
        _ejNode = EJNode.get();
        _thPoolWorker = Executors.newCachedThreadPool(r -> {
            Long pid = _queForkPid.poll();
            Thread th = new Thread(r);
            th.setName("EJNode-worker-" + pid);
            return th;
        });
        _writeLock = _lock.writeLock();
        _readLock = _lock.readLock();
    }

    protected void start(){
        _writeLock.lock();
        try {
            _hasShutdown = false;
            _mapWorker = new HashMap<>();
            _workerPidCnt = 0;
        }finally {
            _writeLock.unlock();
        }
        fork(_clzEntryFirst, _forkParamsFirst);
    }

    protected long fork(Class<? extends NodeEntry> clzEntry, ForkParams forkParams){
        long pid = 0;
        _writeLock.lock();
        try {
            if(_hasShutdown){
                return 0;
            }
            pid = _newWorkerPid();
            if(forkParams == null){   // ensure forkParams is not null
                forkParams = new ForkParamsBuilder().build();
            }
            LoopWorker worker = new LoopWorker(clzEntry, forkParams, pid);
            WorkerWrap wrap = new WorkerWrap(worker);
            _mapWorker.put(pid, wrap);
            // start worker loop
            _queForkPid.offer(pid);
            _thPoolWorker.submit(worker);
        }finally {
            _writeLock.unlock();
        }
        return pid;
    }

    protected void kill(long pid){
        _writeLock.lock();
        try {
            _killUnsafe(pid);
        }finally {
            _writeLock.unlock();
        }
    }
    protected void killOthers(long pidSelf){
        _writeLock.lock();
        try {
            _killOthersUnsafe(pidSelf);
        }finally {
            _writeLock.unlock();
        }
    }

    private void _killUnsafe(long pid){
        WorkerWrap wrap = _mapWorker.get(pid);
        if(wrap != null && !wrap.onShutdown){
            wrap.onShutdown = true;
            wrap.worker.shutdown();
        }
    }

    private void _killAllUnsafe(){
        WorkerWrap[] arr =  _mapWorker.values().toArray(new WorkerWrap[0]);
        for(int i=0; i<arr.length; ++i){
            _killUnsafe(arr[i].worker.pid);
        }
    }

    private void _killOthersUnsafe(long pidSelf){
        WorkerWrap[] arr =  _mapWorker.values().toArray(new WorkerWrap[0]);
        for(int i=0; i<arr.length; ++i){
            long pid = arr[i].worker.pid;
            if(pid != pidSelf){
                _killUnsafe(pid);
            }
        }
    }

    protected void onWorkerLoopStart(long pid){
        _writeLock.lock();
        try {
            WorkerWrap wrap = _mapWorker.get(pid);
            wrap.loopStarted = true;
        }finally {
            _writeLock.unlock();
        }
        _ejNode.onLoopWorkerStart();
    }

    protected void onWorkerLoopExit(long pid){
        int activeWorkerNum = 0;
        _writeLock.lock();
        try {
            WorkerWrap wrap = _mapWorker.remove(pid);
            activeWorkerNum = _mapWorker.size();
        }finally {
            _writeLock.unlock();
        }
        if(activeWorkerNum == 0){   // no activeWorker, shutdown ejnode
            _ejNode.shutdown();
        }
        _ejNode.onLoopWorkerExit();
    }

    protected int workerNum(){
        _readLock.lock();
        try {
            return _mapWorker.size();
        }finally {
            _readLock.unlock();
        }
    }

    public LoopWorker getWorkerByPid(long pid){
        _readLock.lock();
        try {
            WorkerWrap wrap = _mapWorker.get(pid);
            if(wrap != null){
                return wrap.worker;
            }
        }finally {
            _readLock.unlock();
        }
        return null;
    }

    private long _newWorkerPid(){
        return EJConst.WORKER_PID_BASE + (++_workerPidCnt);
    }

    private boolean _hasShutdown = false;
    protected void shutdown(){
        _writeLock.lock();
        try {
            if(_hasShutdown){
                return;
            }
            _hasShutdown = true;
            _thPoolWorker.shutdown();
            _killAllUnsafe();
        }finally {
            _writeLock.unlock();
        }
    }

    static class WorkerWrap{
        protected final LoopWorker worker;
        protected boolean onShutdown = false;
        protected boolean loopStarted = false;
        protected WorkerWrap(LoopWorker worker){
            this.worker = worker;
        }
    }

    @Override
    public void onNodeExit() {

    }


}
