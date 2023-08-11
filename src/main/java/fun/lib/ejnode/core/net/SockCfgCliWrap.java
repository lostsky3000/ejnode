package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.SockCfgClient;
import fun.lib.ejnode.api.net.TcpClient;

public final class SockCfgCliWrap implements SockCfgClient<TcpClient> {

    protected TcpClientWrap clientWrap;
    protected int sendBuff;
    protected int recvBuff;
    protected boolean tcpNoDelay;
    protected boolean keepAlive;

    protected SockCfgCliWrap(TcpClientWrap clientWrap){
        _initDefault();
        this.clientWrap = clientWrap;
    }

    @Override
    public SockCfgClient<TcpClient> sendBuff(int buffSize) {
        sendBuff = Math.max(buffSize, 1);
        return this;
    }

    @Override
    public SockCfgClient<TcpClient> recvBuff(int buffSize) {
        recvBuff = Math.max(buffSize, 1);
        return this;
    }

    @Override
    public SockCfgClient<TcpClient> keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    @Override
    public SockCfgClient<TcpClient> tcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    @Override
    public TcpClient end() {
        return clientWrap;
    }

    private void _initDefault(){
        tcpNoDelay = true;
        keepAlive = true;
        sendBuff = 4096;
        recvBuff = 4096;
    }
}
