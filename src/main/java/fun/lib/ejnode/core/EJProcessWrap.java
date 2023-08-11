package fun.lib.ejnode.core;

import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Process;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.net.TcpChannel;
import fun.lib.ejnode.core.net.TcpChannelWrap;

public final class EJProcessWrap extends Process {

    private LoopWorker _worker;
    private EJWorkerMgr _workerMgr;
    private EJEntryWrap _entryWrap;


    protected EJProcessWrap(LoopWorker worker, EJEntryWrap entryWrap){
        super(Thread.currentThread().getName());
        _worker = worker;
        _entryWrap = entryWrap;

        EJNode ejNode = EJNode.get();
        _workerMgr = ejNode.getWorkerMgr();
    }

    @Override
    public long pid() {
        return _worker.pid;
    }

    @Override
    public void exit() {
        if(_entryWrap.exitCalled()){
            return;
        }
        _entryWrap.callExit();
    }

    @Override
    public long fork(Class<? extends NodeEntry> clzEntry, Object param) {
        if(_entryWrap.exitCalled()){
            return 0;
        }
        return _workerMgr.fork(clzEntry, param);
    }

    @Override
    public long fork(Class<? extends NodeEntry> clzEntry) {
        return fork(clzEntry, null);
    }

    @Override
    public void kill(long pid) {
        if(_entryWrap.exitCalled()){
            return;
        }
        if(pid == pid()){ // kill self
            exit();
        }else{
            _workerMgr.kill(pid);
        }
    }

    @Override
    public void killOthers() {
        if(_entryWrap.exitCalled()){
            return;
        }
        _workerMgr.killOthers(pid());
    }

    @Override
    public boolean send(long pid, Object msg) {
        LoopWorker worker = _workerMgr.getWorkerByPid(pid);
        if(worker != null){
            MsgPriority msgWrap = worker.newMsgPriority();
            msgWrap.type = 2;
            msgWrap.src = pid();
            msgWrap.userData = msg;
            worker.addPriorityMsg(msgWrap);
            return true;
        }
        return false;
    }

    protected void onExit(){
        _entryWrap = null;
        _workerMgr = null;
    }
}
