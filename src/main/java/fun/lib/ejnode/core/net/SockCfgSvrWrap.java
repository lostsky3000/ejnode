package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.SockCfgServer;
import fun.lib.ejnode.api.net.TcpServer;

public final class SockCfgSvrWrap implements SockCfgServer<TcpServer> {
    protected TcpServerWrap svrWrap;
    protected int backLog;
    protected int sendBuff;
    protected int recvBuff;
    protected boolean tcpNoDelay;
    protected boolean keepAlive;

    protected SockCfgSvrWrap(TcpServerWrap svrWrap){
        _initDefault();
        this.svrWrap = svrWrap;
    }
    @Override
    public SockCfgServer<TcpServer> sendBuff(int buffSize) {
        sendBuff = Math.max(buffSize, 1);
        return this;
    }

    @Override
    public SockCfgServer<TcpServer> recvBuff(int buffSize) {
        recvBuff = Math.max(buffSize, 1);
        return this;
    }

    @Override
    public SockCfgServer<TcpServer> backLog(int backLog) {
        this.backLog = backLog;
        return this;
    }

    @Override
    public SockCfgServer<TcpServer> keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    @Override
    public SockCfgServer<TcpServer> tcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    @Override
    public TcpServer end() {
        return svrWrap;
    }


    private void _initDefault(){
        backLog = 1024;
        tcpNoDelay = true;
        keepAlive = true;
        sendBuff = 4096;
        recvBuff = 4096;
    }
}
