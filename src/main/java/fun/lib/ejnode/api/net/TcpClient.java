package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.callback.CbChannelResult;

public interface TcpClient {

    SockCfgClient<TcpClient> soBegin();

    SslCfgClient<TcpClient> sslBegin();

    TcpClient codec(Codec4Client codec);

    TcpClient timeout(long timeoutMs);

    void connect(CbChannelResult cb);

}
