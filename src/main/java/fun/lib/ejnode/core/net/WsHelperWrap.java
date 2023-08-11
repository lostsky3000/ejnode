package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.callback.CbWsConnIn;
import fun.lib.ejnode.api.net.WebsocketHelper;
import fun.lib.ejnode.api.net.WebsocketServer;
import fun.lib.ejnode.core.EJNetWrap;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public final class WsHelperWrap extends WebsocketHelper {

    private final EJNetWrap _netWrap;

    public WsHelperWrap(EJNetWrap netWrap){
        _netWrap = netWrap;
    }

    @Override
    public WebSocketFrame createTextFrame(String text) {
        TextWebSocketFrame frame = new TextWebSocketFrame(text);
        return frame;
    }

    @Override
    public WebsocketServer createServer() {
        WebsocketServer svr = new WebsocketServer(_netWrap);
        return svr;
    }
}
