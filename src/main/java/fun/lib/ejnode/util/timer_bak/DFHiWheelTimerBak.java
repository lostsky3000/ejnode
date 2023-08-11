package fun.lib.ejnode.util.timer_bak;

import fun.lib.ejnode.util.container.DFPooledLinkedList;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class DFHiWheelTimerBak {

    private final Wheel _rootWheel;
    private final ReentrantReadWriteLock _rwLock;
    private final ReentrantReadWriteLock.ReadLock _readLock;
    private final ReentrantReadWriteLock.WriteLock _writeLock;

    private final DelayQueue<Bucket> _queueBucket;
    private final DFHiWheelTimerCb _timeoutCb;

    private final DFPooledLinkedList<TimeoutTask> _taskPool = new DFPooledLinkedList<TimeoutTask>();
    private final ReentrantLock _lockTaskPool = new ReentrantLock();

    public DFHiWheelTimerBak(long tickMs, int wheelSize, DFHiWheelTimerCb timeoutCb){
        _rwLock = new ReentrantReadWriteLock();
        _readLock = _rwLock.readLock();
        _writeLock = _rwLock.writeLock();
        _timeoutCb = timeoutCb;
        //
        _writeLock.lock();
        try {
            _queueBucket = new DelayQueue<Bucket>();
            _rootWheel = new Wheel(tickMs, wheelSize, getTimeMs(), _queueBucket, 0);
        }finally {
            _writeLock.unlock();
        }
    }

    public void add(DFTimeoutBak timeout, long delayMs, int execTimes){
//        TimeoutTask task = new TimeoutTask(timeout, delayMs, getTimeMs(), execTimes, false);
        TimeoutTask task = _allocTimeoutTask();
        task.reset(timeout, delayMs, getTimeMs(), execTimes);
        _readLock.lock();
        try{
//            long tmNow = getTimeMs();
            addTimeout(task, false);
//            System.out.println("timerAddTimeout: rootWheelCurTime=" + _rootWheel._currentTime + ", tmNow=" + tmNow
//                + ", expiration="+(delayMs + tmNow));
        }finally{
            _readLock.unlock();
        }
    }

    public void add(DFTimeoutBak timeout, long delayMs) {
        add(timeout, delayMs, 1);
    }

    protected void addTimeout(TimeoutTask task, boolean fromInner){
        if(!_rootWheel.add(task)){
            // already expired, callback
            //System.out.println("timerCbTm: " + System.currentTimeMillis());
            DFTimeoutBak timeout = task.timeout;
            _timeoutCb.onTimeout(timeout);
            if(fromInner && task.fromPool){
                if(task.execTimesInner == -1){
                    task.execTimesInner = task.execTimes;
                }
                if(--task.execTimesInner > 0){  //readd

                }else{   // recycle

                }
            }else{
                if(--task.execTimes > 0){  //readd

                }
            }
        }
    }

    private TimeoutTask _allocTimeoutTask(){
        TimeoutTask task;
        if(_lockTaskPool.tryLock()){
            try {
                task = _taskPool.poll();
                if(task == null){
                    task = new TimeoutTask(false);
                }
            }finally {
                _lockTaskPool.unlock();
            }
        }else{
            task = new TimeoutTask(false);
        }
        return task;
    }

    public void advanceClock(long waitMs){
        Bucket bucket = null;
        try {
            bucket = _queueBucket.poll(waitMs, TimeUnit.MILLISECONDS);
            //bucket = _queueBucket.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(bucket != null){
            _writeLock.lock();
            try {
                while(bucket != null){
//                    long tmNow = getTimeMs();
//                    long expire = bucket.getExpiration();
//                    System.out.println("========== advanceClock: tmLeft=" + (expire - tmNow) + ", depth="+bucket.wheel.depth
//                            + ", bucketExpire=" + expire + ", tmNow=" + tmNow);
//                    _rootWheel.advanceClock(expire);
                    //
                    _rootWheel.advanceClock(bucket.getExpiration());
                    bucket.flush(this);
                    bucket = _queueBucket.poll();
                }
            }finally {
                _writeLock.unlock();
            }
        }
        // check taskPool
        _checkExpandTaskPool();
    }

    public void initTaskPoolInAdvanceThread(){
        _lockTaskPool.lock();
        try {
            for(int i=0; i<1000; ++i){
                TimeoutTask task = new TimeoutTask(true);
                _taskPool.offer(task);
            }
        }finally {
            _lockTaskPool.unlock();
        }
    }

    private void _checkExpandTaskPool(){
        if(_lockTaskPool.tryLock()){
            try {
                int add = 1000 - _taskPool.size();
                if(add > 0){
                    add = Math.min(add, 10);
                    for(int i=0; i<add; ++i){
                        TimeoutTask task = new TimeoutTask(true);
                        _taskPool.offer(task);
                    }
                }
            }finally {
                _lockTaskPool.unlock();
            }
        }
    }

    static class Wheel {
        private final long _tickMs;
        private final int _wheelSize;
        private final long _interval;
        private long _currentTime;

        private volatile Wheel _overflowWheel = null;
        private final Bucket[] _buckets;
        private final DelayQueue<Bucket> _queueBucket;

        protected final int depth;  // debug

        protected Wheel(long tickMs, int wheelSize, long startMs, DelayQueue<Bucket> queueBucket, int preDepth){
            this.depth = preDepth + 1;
            _tickMs = tickMs;
            _wheelSize = wheelSize;
            _interval = tickMs * wheelSize;
            _currentTime = startMs - (startMs % tickMs);
            _buckets = new Bucket[_wheelSize];
            for(int i=0; i<_wheelSize; ++i){
                _buckets[i] = new Bucket(this);
            }
            _queueBucket = queueBucket;
        }

        protected boolean add(TimeoutTask task){
            long expiration = task.expirationMs;
            if(expiration < _currentTime + _tickMs){    // 已经过期
                return false;
            }
            if(expiration < _currentTime + _interval){  // 可以放在当前wheel
                long id = expiration / _tickMs;
                Bucket bucket = _buckets[(int)(id % (long)_wheelSize)];
                bucket.add(task);
                if(bucket.setExpiration(id * _tickMs)){
                    //add to delayQueue
                    _queueBucket.offer(bucket);
                }
            }else{  // 溢出，放入上一层wheel
                if(_overflowWheel == null){  //尝试
                    _addOverflowWheel();
                }
                _overflowWheel.add(task);
            }
            return true;
        }

        private void _addOverflowWheel(){
            synchronized (this){
                if(_overflowWheel == null){
                    _overflowWheel = new Wheel(_interval, _wheelSize, _currentTime, _queueBucket, depth);
                }
            }
        }

        protected void advanceClock(long timeMs){
            if(timeMs >= _currentTime + _tickMs){
                _currentTime = timeMs - (timeMs % _tickMs);
                if(_overflowWheel != null){
                    _overflowWheel.advanceClock(_currentTime);
                }
            }
        }

    }

    static class Bucket implements Delayed {
        protected final TimeoutTask taskRoot;
        private final AtomicLong _expiration;
        protected final Wheel wheel;

        protected Bucket(Wheel wheel){
            taskRoot = new TimeoutTask(null,-1, 0, 0, false);
            taskRoot.prev = taskRoot;
            taskRoot.next = taskRoot;
            taskRoot.bucket = this;
            //
            _expiration = new AtomicLong(-1);
            //
            this.wheel = wheel;
        }
        protected void add(TimeoutTask cur){
            synchronized (this){
                synchronized (cur){
                    TimeoutTask tail = taskRoot.prev;
                    tail.next = cur;
                    taskRoot.prev = cur;
                    //
                    cur.prev = tail;
                    cur.next = taskRoot;
                    cur.bucket = this;
                }
            }
        }

        protected void flush(DFHiWheelTimerBak timer){
            synchronized (this){
                TimeoutTask head = taskRoot.next;
                while(head != taskRoot){
                    _remove(head);            // remove
                    timer.addTimeout(head, true);   // reinsert
                    head = taskRoot.next;
                }
                _expiration.set(-1);
            }
        }

        private void _remove(TimeoutTask task){
            task.next.prev = task.prev;
            task.prev.next = task.next;
            task.prev = null;
            task.next = null;
            task.bucket = null;
        }

        protected boolean setExpiration(long expiration){
            return _expiration.getAndSet(expiration) != expiration;
        }
        protected Long getExpiration(){
            return _expiration.get();
        }

        // implement Delayed
        public long getDelay(TimeUnit unit) {
//            long tmNow = getTimeMs();
//            long exp = getExpiration();
//            long delay = exp - tmNow;
//            System.out.println("getDelay, delay="+delay+", expire="+exp+", now="+tmNow);
//            if(delay <= 0){
//                int n = 1;
//            }
//            return unit.convert(Math.max(delay, 0), TimeUnit.MILLISECONDS);
            return unit.convert(Math.max(getExpiration() - getTimeMs(), 0), TimeUnit.MILLISECONDS);
        }
        // implement Delayed
        public int compareTo(Delayed o) {
            Bucket other = (Bucket) o;
            long thisVal = getExpiration();
            long otherVal = other.getExpiration();
            if(thisVal < otherVal){
                return -1;
            }else if(thisVal > otherVal){
                return 1;
            }
            return 0;
        }
    }

    static class TimeoutTask{
        protected final boolean fromPool;
        protected volatile int execTimes;
        protected int execTimesInner;
        protected long delay;
        protected long expirationMs;
        protected DFTimeoutBak timeout;
        //
        protected TimeoutTask prev;
        protected TimeoutTask next;
        protected Bucket bucket;

        protected TimeoutTask(DFTimeoutBak timeout, long delay, long now, int execTimes, boolean fromPool){
            reset(timeout, delay, now, execTimes);
            this.fromPool = fromPool;
            execTimesInner = -1;
        }
        protected TimeoutTask(boolean fromPool){
            this.fromPool = fromPool;
            execTimesInner = -1;
        }
        protected void reset(DFTimeoutBak timeout, long delay, long now, int execTimes){
            this.timeout = timeout;
            this.delay = delay;
            this.expirationMs = now + delay;
            this.execTimes = execTimes;
        }
    }

    static long getTimeMs(){
        return System.nanoTime() / 1000000;
        //return System.currentTimeMillis();
    }

    public interface DFHiWheelTimerCb{
        void onTimeout(DFTimeoutBak cb);
    }
}
