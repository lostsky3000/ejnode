package fun.lib.ejnode.core;

import fun.lib.ejnode.api.TimerFuture;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.api.callback.*;
import fun.lib.ejnode.util.container.DFPooledLinkedList;
import fun.lib.ejnode.util.timer.DFTimeout;

public final class EJTimerWrap implements Timer {

    private LoopWorker _worker;
    private DFPooledLinkedList<TimeoutTask> _timeoutTaskPool = new DFPooledLinkedList<>();
    private EJEntryWrap _entryWrap;

    private EJWorkerMgr _workerMgr;
    private EJTimerCore _timerCore;

    protected EJTimerWrap(LoopWorker worker, EJEntryWrap entryWrap){
        _worker = worker;
        _entryWrap = entryWrap;
        EJNode ejNode = EJNode.get();
        _workerMgr = ejNode.getWorkerMgr();
        _timerCore = ejNode.getTimer();
    }

    @Override
    public TimerFuture nextTick(Object userData, CbCommonUD cb) {
        if(_entryWrap.exitCalled()){
            return null;
        }
        return _worker.addNextTick(null, cb, userData);
    }
    @Override
    public TimerFuture nextTick(CbCommon cb) {
        if(_entryWrap.exitCalled()){
            return null;
        }
        return _worker.addNextTick(cb, null, null);
    }

    @Override
    public TimerFuture immediate(Object userData, CbCommonUD cb) {
        if(_entryWrap.exitCalled()){
            return null;
        }
        return _worker.addImmediate(null, cb, userData);
    }

    @Override
    public TimerFuture immediate(CbCommon cb) {
        if(_entryWrap.exitCalled()){
            return null;
        }
        return _worker.addImmediate(cb, null, null);
    }

    @Override
    public TimerFuture timeout(long delay, Object userData, CbCommonUD cb) {
        return _doTimeout(null, cb, delay, userData);
    }

    @Override
    public TimerFuture timeout(long delay, CbCommon cb) {
        return _doTimeout(cb, null, delay,null);
    }
    private TimerFuture _doTimeout(CbCommon cb, CbCommonUD cbUd, long delay, Object userData){
        if(_entryWrap.exitCalled()){
            return null;
        }
        delay = Math.max(delay, 0);
        TimeoutTask task = _newTimeoutTask();
        task.reset(1, delay, cb, cbUd, null, null, userData);
        task.repeat = 1;
        _timerCore.add(task, delay);
        //
        TimerTaskWrap wrap = new TimerTaskWrap(task);
        task.taskWrap = wrap;

//        System.out.println("_doTimeout: "+task.toString());

        return wrap;
    }

    @Override
    public TimerFuture schedule(long interval, Object userData, CbScheduleUD cb) {
        return _doSchedule(null, cb, interval, userData);
    }
    @Override
    public TimerFuture schedule(long interval, CbSchedule cb) {
        return _doSchedule(cb, null, interval, null);
    }

    private TimerFuture _doSchedule(CbSchedule cb, CbScheduleUD cbUd, long interval, Object userData){
        if(_entryWrap.exitCalled()){
            return null;
        }
        interval = Math.max(interval, 1);
        TimeoutTask task = _newTimeoutTask();
        task.reset(2, interval, null,null, cb, cbUd, userData);
        // calc repeat
        int repeat = _calcScheduleRepeat(interval);
        task.repeat = repeat;
        _timerCore.add(task, interval, repeat);

        TimerTaskWrap wrap = new TimerTaskWrap(task);
        task.taskWrap = wrap;
        return wrap;
    }

    private int _calcScheduleRepeat(long interval){
        long repeat = 60*1000 / interval;
        repeat = Math.max(repeat, 1);
        return (int)repeat;
    }

    private TimeoutTask _newTimeoutTask(){
        TimeoutTask task = _timeoutTaskPool.poll();
        if(task == null){  // no pooled task, create
            task = new TimeoutTask(_worker.pid, _workerMgr);
        }
        return task;
    }

    protected void onTimerCb(Object userData){
        TimeoutTask task = (TimeoutTask)userData;
        TimerTaskWrap taskWrap = task.taskWrap;
        int taskType = task.taskType;
        boolean recycleTask = true;
        boolean canceled = taskWrap.isCanceled();

//        System.out.println("onTimerCb, taskType="+taskType+", "+task.toString());

        if(taskType == 2){   // schedule
            if(!canceled){
                try {
                    if(task.cbSchedule != null){
                        task.cbSchedule.onSchedule(taskWrap);
                    }else{
                        task.cbScheduleUd.onSchedule(taskWrap, task.userData);
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
            recycleTask = false;
            if(--task.repeat == 0){
                if(!canceled){ // readd
                    long delay = task.delay;
                    int repeat = _calcScheduleRepeat(delay);
                    task.repeat = repeat;
                    _timerCore.add(task, delay, repeat);
                }else {
                    recycleTask = true;
                }
            }
        }else if(taskType == 1){  // timeout
            if(!canceled){
                try {
                    if(task.cb != null){
                        task.cb.onCallback();
                    }else{
                        if(task.cbUd == null){
                            int n = 1;
                        }
                        task.cbUd.onCallback(task.userData);
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
                taskWrap.setDone();  // mark task has done
            }
        }
        if(recycleTask){
            task.clear();
            taskWrap.task = null;
            _timeoutTaskPool.offer(task);
        }
    }

    static class TimeoutTask implements DFTimeout{
        protected int taskType;
        protected long delay;
        protected CbCommon cb;
        protected CbCommonUD cbUd;
        protected CbSchedule cbSchedule;
        protected CbScheduleUD cbScheduleUd;

        protected Object userData;
        protected int repeat;
        private final long _workerPid;
        private final EJWorkerMgr _workerMgr;
        protected TimerTaskWrap taskWrap;

        protected TimeoutTask(long pid, EJWorkerMgr workerMgr){
            _workerPid = pid;
            _workerMgr = workerMgr;
        }

        protected void reset(int taskType, long delay, CbCommon cb, CbCommonUD cbUd,
                             CbSchedule cbSchedule, CbScheduleUD cbScheduleUd, Object userData){
            this.taskType = taskType;
            this.delay = delay;
            this.cb = cb;
            this.cbUd = cbUd;
            this.cbSchedule = cbSchedule;
            this.cbScheduleUd = cbScheduleUd;
            this.userData = userData;
        }
        protected void clear(){
            cb = null;
            cbUd = null;
            cbSchedule = null;
            cbScheduleUd = null;
            userData = null;
        }

        // called from timer-callback-thread, notify loopWorker thread
        @Override
        public void onTimeout() {
            LoopWorker worker = _workerMgr.getWorkerByPid(_workerPid);
            if(worker != null){
                MsgPriority msg = worker.newMsgPriority();
                msg.type = 1;
                msg.userData = this;
                worker.addPriorityMsg(msg);
            }
        }
    }

    static class TimerTaskWrap implements TimerFuture {
        protected TimeoutTask task;
        private boolean _canceled;
        private boolean _isDone;
        protected TimerTaskWrap(TimeoutTask task){
            this.task = task;
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

    protected void onExit(){
        _entryWrap = null;
        _timerCore = null;
        _workerMgr = null;
    }
}
