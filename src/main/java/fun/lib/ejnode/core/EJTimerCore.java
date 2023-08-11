package fun.lib.ejnode.core;

import fun.lib.ejnode.util.timer.DFHiWheelTimer;
import fun.lib.ejnode.util.timer.DFTimeout;
import fun.lib.ejnode.util.timer.DFTimerCb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class EJTimerCore implements DFTimerCb, EJNodeLife{
    protected final DFHiWheelTimer _timer;
    protected final ExecutorService _thPoolTimer;
    protected final ExecutorService _thPoolCb;
    private final LoopTimer _loopTimer;

    protected EJTimerCore(long tickMs, int wheelSize, long waitMs){
        _timer = new DFHiWheelTimer(tickMs, wheelSize, this);
        _thPoolTimer = Executors.newFixedThreadPool(1, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread th = new Thread(r);
                th.setName("EJNode-timer");
                return th;
            }
        });
        _thPoolCb = Executors.newFixedThreadPool(1, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread th = new Thread(r);
                th.setName("EJNode-timer-callback");
                return th;
            }
        });
        _loopTimer = new LoopTimer(_timer, waitMs);
    }

    protected void start(){
        _thPoolTimer.submit(_loopTimer);
    }

    protected void shutdown(){
        _loopTimer.shutdown();
        _thPoolTimer.shutdown();
    }

    protected void add(DFTimeout timeout, long delayMs){
        _timer.add(timeout, delayMs);
    }
    protected void add(DFTimeout timeout, long delayMs, int repeat){
        _timer.add(timeout, delayMs, repeat);
    }

    @Override
    public void onTimerCb(DFTimeout timeout) {

        _thPoolCb.submit(timeout::onTimeout);
    }

    @Override
    public void onNodeExit() {
        _thPoolCb.shutdown();
    }

    static class LoopTimer implements Runnable{
        private volatile boolean _onLoop = false;
        private final DFHiWheelTimer _timer;
        private final long _waitMs;
        private LoopTimer(DFHiWheelTimer timer, long waitMs){
            _timer = timer;
            _waitMs = waitMs;
        }
        public void run() {
            EJNode ejNode = EJNode.get();
            ejNode.onLoopInnerStart(EJNode.LOOP_TYPE_TIMER);
            //
            _onLoop = true;
            while (_onLoop){
                try {
                    _timer.advanceClock(_waitMs);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
            ejNode.onLoopInnerExit(EJNode.LOOP_TYPE_TIMER);
        }
        protected void shutdown(){
            _onLoop = false;
            _timer.add(() -> {}, EJConst.TIMER_TICK_MS);
        }
    }
}


