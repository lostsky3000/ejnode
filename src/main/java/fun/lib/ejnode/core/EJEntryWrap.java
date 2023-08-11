package fun.lib.ejnode.core;

import fun.lib.ejnode.api.*;
import fun.lib.ejnode.api.Process;


public final class EJEntryWrap{
    private LoopWorker _worker;
    protected NodeEntry entry;

    protected EJProcessWrap processWrap;
    protected EJTimerWrap timerWrap;
    protected EJNetWrap netWrap;
    protected EJEnvWrap envWrap;
    protected EJLoggerWrap loggerWrap;
    protected EJDbWrap dbWrap;

    private boolean _exitCalled;

    protected NodeCtxWrap nodeCtxWrap;

    protected EJEntryWrap(NodeEntry entry, LoopWorker worker){
        this.entry = entry;
        _worker = worker;

        processWrap = new EJProcessWrap(worker, this);
        timerWrap = new EJTimerWrap(worker, this);
        netWrap = new EJNetWrap(worker, this);
        loggerWrap = new EJLoggerWrap();
        envWrap = new EJEnvWrap();
        _exitCalled = false;
        //
        dbWrap = new EJDbWrap(netWrap, timerWrap, loggerWrap);
        //
        nodeCtxWrap = new NodeCtxWrap(timerWrap, loggerWrap, processWrap, netWrap, dbWrap, envWrap, entry);

        netWrap.setNodeCtx(nodeCtxWrap);
        netWrap.setTimerWrap(timerWrap);
    }

    protected void callExit(){
        _exitCalled = true;
        _worker.callExit();
    }
    protected boolean exitCalled(){
        return _exitCalled;
    }

    protected void onExit(){
        timerWrap.onExit();
        processWrap.onExit();
        netWrap.onExit();
        dbWrap.onExit();
    }

    protected void onMsgFromOthers(long pidSrc, Object msg){
        try {
            entry.onMessage(pidSrc, msg);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    protected void onLogBatchEnd(){
        loggerWrap.onLogBatchEnd();
    }
    protected void onFrameEnd(){
        loggerWrap.onLogBatchEnd();
        dbWrap.onFrameEnd();
    }

    static class NodeCtxWrap extends NodeContext{
        protected NodeCtxWrap(Timer timer, Logger logger, Process process, Net net, Db db, Environment environment, NodeEntry entry) {
            super(timer, logger, process, net, db, environment, entry);
        }
        @Override
        public void shutdown() {
            EJNode.get().shutdown();
        }
    }
}
