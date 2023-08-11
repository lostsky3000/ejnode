package fun.lib.ejnode.api;

import fun.lib.ejnode.api.callback.CbChannelReadFull;
import fun.lib.ejnode.api.net.*;

public abstract class Net {

    public abstract HttpHelper http();

    public abstract WebsocketHelper websocket();

    public abstract TcpServer createServer(int port);

    public abstract TcpServer createServer(String host, int port);

    public abstract TcpClient createClient(String host, int port);

}
