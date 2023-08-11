package fun.lib.ejnode.core;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.util.container.DFObjPool;
import fun.lib.ejnode.util.container.DFPooledLinkedList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class EJLoggerCore implements Runnable{

    private final ExecutorService _thPool;
    private boolean _onLoop;
    private final AtomicInteger _msgCnt;
    private final ConcurrentLinkedQueue<MsgLog> _queueMsg;
    private final ReentrantLock _lock;
    private final Condition _cond;

    private final DFObjPool<MsgLog> _msgPool;

    protected final int _logLevel;

    private static final int STR_BUFF_MIN_LEN = 1000;
    private static final int BUFF_CHANGE_BIAS = 2000;

    protected EJLoggerCore(int logLevel){
        _logLevel = logLevel;
        _msgCnt = new AtomicInteger(0);
        _queueMsg = new ConcurrentLinkedQueue<>();

        _msgPool = new DFObjPool<>(1000);

        _lock = new ReentrantLock();
        _cond = _lock.newCondition();
        _thPool = Executors.newFixedThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread th = new Thread(r);
                th.setName("EJNode-logger");
                return th;
            }
        });
    }

    @Override
    public void run() {
        EJNode ejNode = EJNode.get();
        ejNode.onLoopInnerStart(EJNode.LOOP_TYPE_LOGGER);
        //
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        _onLoop = true;
        MsgLog msg = null;
        int procMsgCnt = 0;
        MsgLog.LogItem item = null;
        int bufLenInit = STR_BUFF_MIN_LEN;
        boolean shutdownCalled = false;
        while (_onLoop){
            procMsgCnt = 0;
            while ((msg=_queueMsg.poll()) != null){
                ++procMsgCnt;
                StringBuilder sb = new StringBuilder(bufLenInit);
                final String strNow = sdf.format(Calendar.getInstance().getTime());
                while ((item=msg.pollItem()) != null){
                    if(item.level >= _logLevel){
                        sb.append(strNow);
                        sb.append(LEVEL_TAGS[item.level-1]);
                        if(item.tag != null){
                            sb.append('[');
                            sb.append(item.tag.trim());
                            sb.append(']');
                        }
                        if(item.str != null){
                            sb.append(' ');
                            sb.append(item.str.trim());
                        }else{

                        }
                        sb.append('\n');
                    }else if(item.level == -1){  // shutdown signal
                        shutdownCalled = true;
                        _onLoop = false;
                        break;
                    }
                    msg.recycleItem(item);
                }
                _msgPool.recycle(msg);
                if(shutdownCalled){
                    break;
                }
                //
                int bufLenRet = sb.length();
                System.out.print(sb.toString());
                if(bufLenRet > bufLenInit){
                    bufLenInit = bufLenRet;
                }else{
                    int bias = bufLenInit - bufLenRet;
                    if(bias >= BUFF_CHANGE_BIAS){
                        bufLenInit = Math.max(bufLenRet, STR_BUFF_MIN_LEN);
                    }
                }
            }
            if(procMsgCnt > 0){
                _msgCnt.addAndGet(-procMsgCnt);
            }
            if(shutdownCalled){
                break;
            }
            //
            _lock.lock();
            try {
                int msgNum = _msgCnt.get();
                if(msgNum == 0){
                    _cond.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                _lock.unlock();
            }
        }
        //
        ejNode.onLoopInnerExit(EJNode.LOOP_TYPE_LOGGER);
    }

    protected MsgLog newMsgLog(){
        MsgLog msg = _msgPool.newObj();
        if(msg == null){
            msg = new MsgLog();
        }
        return msg;
    }

    protected void addLogMsg(String tag, String str, int level){
        MsgLog msg = newMsgLog();
        msg.addItem(tag, str, level);
        addLogMsg(msg);
    }

    protected void addLogMsg(MsgLog msg){
        int msgNum = _msgCnt.incrementAndGet();
        _queueMsg.offer(msg);
        if(msgNum == 1){
            _lock.lock();
            try {
                _cond.signal();
            }finally {
                _lock.unlock();
            }
        }
    }

    protected void start(){
        _thPool.submit(this);
    }

    protected void shutdown(){
        _thPool.shutdown();
        addLogMsg(null, "LOGGER SHUTDOWN SIGNAL", -1);
    }

    // trace, debug, info, warn, error, fatal
    private static final String[] LEVEL_TAGS= {"[T]","[D]","[I]","[W]","[E]","[F]"};

    public void logInfo(String str){
        addLogMsg(null, str, Logger.LEVEL_INFO);
    }
    public void logErr(String str){
        addLogMsg(null, str, Logger.LEVEL_ERROR);
    }
}
