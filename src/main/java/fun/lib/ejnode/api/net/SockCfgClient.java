package fun.lib.ejnode.api.net;

public interface SockCfgClient<T> {

    SockCfgClient<T> sendBuff(int buffSize);

    SockCfgClient<T> recvBuff(int buffSize);

    SockCfgClient<T> keepAlive(boolean keepAlive);

    SockCfgClient<T> tcpNoDelay(boolean tcpNoDelay);

    T end();

}