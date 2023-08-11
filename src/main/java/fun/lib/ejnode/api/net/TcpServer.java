package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.callback.CbServerResult;

public interface TcpServer {

    TcpServer codec(Codec4Server codec);

    SockCfgServer<TcpServer> soBegin();

    TcpServerFuture listen();

    TcpServerFuture listen(CbServerResult cb);
}
