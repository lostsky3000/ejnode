package fun.lib.ejnode.api.net;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.callback.CbCommon;
import fun.lib.ejnode.api.callback.CbWsMessage;

public interface WsSocket {

    void close();

    void onMessage(CbWsMessage cb) throws StatusIllegalException;

    void onClose(CbCommon cb) throws StatusIllegalException;

    void send(String str);

    void sendThenClose(String str);

}
