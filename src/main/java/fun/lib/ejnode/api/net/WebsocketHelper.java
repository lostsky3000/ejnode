package fun.lib.ejnode.api.net;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public abstract class WebsocketHelper {

    public abstract WebSocketFrame createTextFrame(String text);

    public abstract WebsocketServer createServer();
}
