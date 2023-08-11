package fun.lib.ejnode.util.timer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DFHiWheelTimer {

    private final DFTimerCb _timerCb;

    private final DelayQueue<Bucket> _queueTask;
    private final AtomicInteger _taskCounter;
    private final Wheel _rootWheel;

    // Locks used to protect data structures while ticking
    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock _readLock = _lock.readLock();
    private final ReentrantReadWriteLock.WriteLock _writeLock = _lock.writeLock();

    public DFHiWheelTimer(long tickMs, int wheelSize, DFTimerCb timerCb) {
        _timerCb = timerCb;
        _queueTask = new DelayQueue<Bucket>();
        _taskCounter = new AtomicInteger(0);
        _rootWheel = new Wheel(
                tickMs,
                wheelSize,
                s_getTimeMs(),
                _taskCounter,
                _queueTask
        );
    }

    public void add(DFTimeout cb, long delayMs){
        add(cb, delayMs, 1);
    }
//    public void add(TimerTask timerTask) {
    public void add(DFTimeout cb, long delayMs, int repeat) {
        _readLock.lock();
        try {
            addTask(new TaskWrap(cb, delayMs, s_getTimeMs(), repeat));
        } finally {
            _readLock.unlock();
        }
    }

    protected void addTask(TaskWrap task) {

//        if (!timingWheel.add(timerTaskEntry)) {
//            //Already expired or cancelled
//            if (!timerTaskEntry.cancelled()) {
//                taskExecutor.submit(timerTaskEntry.timerTask);
//            }
//        }
        // vars in taskWrap will be visible in advanceClock() thread,
        // because bucket.add() & bucket.flush() are both synchronized methods
        // that match `happens-before rule`
        while(!_rootWheel.add(task)){
            //callback
            _timerCb.onTimerCb(task.timeoutCb);
            //check repeat
            if(--task.repeat <= 0){
                break;
            }
            // readd, update expiration
            task.expirationMs += task.delayMs;
        }
    }

    /**
     *
     */
    public boolean advanceClock(long timeoutMs) throws InterruptedException {
        Bucket bucket = _queueTask.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (bucket != null) {
            _writeLock.lock();
            try {
                while (bucket != null) {
                    _rootWheel.advanceClock(bucket.getExpiration());
                    bucket.flush(this);
                    bucket = _queueTask.poll();
                }
            } finally {
                _writeLock.unlock();
            }
            return true;
        } else {
            return false;
        }
    }

    public int size() {
        return _taskCounter.get();
    }

    private static long s_getTimeMs(){
//        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        return System.nanoTime()/1000000;
    }


    /**
     *
     */
    static class Wheel {
        private final long _tickMs;
        private final int _wheelSize;
        private final AtomicInteger _taskCounter;
        private final DelayQueue<Bucket> _queueTask;
        private final long _interval;
        private final Bucket[] _buckets;
        private long _currentTimeMs;

        // overflowWheel can potentially be updated and read by two concurrent threads through add().
        // Therefore, it needs to be volatile due to the issue of Double-Checked Locking pattern with JVM
        private volatile Wheel _overflowWheel = null;
        Wheel(
                long tickMs,
                int wheelSize,
                long startMs,
                AtomicInteger taskCounter,
                DelayQueue<Bucket> queue
        ) {
            _tickMs = tickMs;
            _wheelSize = wheelSize;
            _taskCounter = taskCounter;
            _queueTask = queue;
            _buckets = new Bucket[wheelSize];
            _interval = tickMs * wheelSize;
            // rounding down to multiple of tickMs
            _currentTimeMs = startMs - (startMs % tickMs);

            for (int i = 0; i < _buckets.length; i++) {
                _buckets[i] = new Bucket(taskCounter);
            }
        }

        private synchronized void addOverflowWheel() {
            if (_overflowWheel == null) {
                _overflowWheel = new Wheel(
                        _interval,
                        _wheelSize,
                        _currentTimeMs,
                        _taskCounter,
                        _queueTask
                );
            }
        }

        public boolean add(TaskWrap task) {
            long expiration = task.expirationMs;
            if (expiration < _currentTimeMs + _tickMs) {
                // Already expired
                return false;
            } else if (expiration < _currentTimeMs + _interval) {
                // Put in its own bucket
                long virtualId = expiration / _tickMs;
                int bucketId = (int) (virtualId % (long) _wheelSize);
                Bucket bucket = _buckets[bucketId];
                bucket.add(task);
                // Set the bucket expiration time
                if (bucket.setExpiration(virtualId * _tickMs)) {
                    // The bucket needs to be enqueued because it was an expired bucket
                    // We only need to enqueue the bucket when its expiration time has changed, i.e. the wheel has advanced
                    // and the previous buckets gets reused; further calls to set the expiration within the same wheel cycle
                    // will pass in the same value and hence return false, thus the bucket with the same expiration will not
                    // be enqueued multiple times.
                    _queueTask.offer(bucket);
                }

                return true;
            } else {
                // Out of the interval. Put it into the parent timer
                if (_overflowWheel == null) addOverflowWheel();
                return _overflowWheel.add(task);
            }
        }

        public void advanceClock(long timeMs) {
            if (timeMs >= _currentTimeMs + _tickMs) {
                _currentTimeMs = timeMs - (timeMs % _tickMs);

                // Try to advance the clock of the overflow wheel if present
                if (_overflowWheel != null) _overflowWheel.advanceClock(_currentTimeMs);
            }
        }
    }

    /**
     *
     */
    static class Bucket implements Delayed {
        private final AtomicInteger _taskCounter;
        private final AtomicLong _expiration;

        // root.next points to the head
        // root.prev points to the tail
        private final TaskWrap _root;
        Bucket(AtomicInteger taskCounter) {
            this._taskCounter = taskCounter;
            this._expiration = new AtomicLong(-1L);
            this._root = new TaskWrap(null, -1L, 0, 0);
            this._root.next = _root;
            this._root.prev = _root;
        }

        public boolean setExpiration(long expirationMs) {
            return _expiration.getAndSet(expirationMs) != expirationMs;
        }

        public long getExpiration() {
            return _expiration.get();
        }

        public void add(TaskWrap task) {
            boolean done = false;
            while (!done) {
                // Remove the timer task entry if it is already in any other list
                // We do this outside of the sync block below to avoid deadlocking.
                // We may retry until timerTaskEntry.list becomes null.
                task.remove();

                synchronized (this) {
                    synchronized (task) {
                        if (task.bucket == null) {
                            // put the timer task entry to the end of the list. (root.prev points to the tail entry)
                            TaskWrap tail = _root.prev;
                            task.next = _root;
                            task.prev = tail;
                            task.bucket = this;
                            tail.next = task;
                            _root.prev = task;
                            _taskCounter.incrementAndGet();
                            done = true;
                        }
                    }
                }
            }
        }

        public synchronized void remove(TaskWrap task) {
            synchronized (task) {
                if (task.bucket == this) {
                    task.next.prev = task.prev;
                    task.prev.next = task.next;
                    task.next = null;
                    task.prev = null;
                    task.bucket = null;
                    _taskCounter.decrementAndGet();
                }
            }
        }

        public synchronized void flush(DFHiWheelTimer timer) {
            TaskWrap head = _root.next;
            while (head != _root) {
                remove(head);
                timer.addTask(head);
                head = _root.next;
            }
            _expiration.set(-1L);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(Math.max(getExpiration() - s_getTimeMs(), 0), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            Bucket other = (Bucket) o;
            return Long.compare(getExpiration(), other.getExpiration());
        }
    }

    static class TaskWrap {
        protected final DFTimeout timeoutCb;
        protected final long delayMs;
        protected long expirationMs;
        protected int repeat;
        volatile Bucket bucket;
        TaskWrap next;
        TaskWrap prev;

        public TaskWrap(
                DFTimeout timeoutCb,
                long delayMs,
                long now,
                int repeat
        ) {
            this.timeoutCb = timeoutCb;
            this.delayMs = delayMs;
            this.expirationMs = now + delayMs;
            this.repeat = repeat;
        }

        public void remove() {
            Bucket currentList = bucket;
            // If remove is called when another thread is moving the entry from a task entry list to another,
            // this may fail to remove the entry due to the change of value of list. Thus, we retry until the list becomes null.
            // In a rare case, this thread sees null and exits the loop, but the other thread insert the entry to another list later.
            while (currentList != null) {
                currentList.remove(this);
                currentList = bucket;
            }
        }
    }

}
