package fun.lib.ejnode.api.net;

public interface SockCfgServer<T> {

    SockCfgServer<T> sendBuff(int buffSize);

    SockCfgServer<T> recvBuff(int buffSize);

    SockCfgServer<T> backLog(int backLog);

    SockCfgServer<T> keepAlive(boolean keepAlive);

    SockCfgServer<T> tcpNoDelay(boolean tcpNoDelay);

    T end();
}
