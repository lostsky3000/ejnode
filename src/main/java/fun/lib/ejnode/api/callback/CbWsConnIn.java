package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.WsSocket;

public interface CbWsConnIn {
    void onConnIn(WsSocket socket);
}
