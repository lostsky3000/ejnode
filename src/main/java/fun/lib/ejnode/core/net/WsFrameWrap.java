package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.WebsocketFrame;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public final class WsFrameWrap extends WebsocketFrame implements IoDataInWrap {
    private final WebSocketFrame _rawFrame;
    public final boolean isText;

    private String _text;

    public WsFrameWrap(WebSocketFrame rawFrame, boolean isText){
        _rawFrame = rawFrame;
        this.isText = isText;
    }

    @Override
    public boolean isText() {
        return isText;
    }

    @Override
    public String text() {
        if(isText){
            if(_text == null){
                _text = ((TextWebSocketFrame)_rawFrame).text();
            }
            return _text;
        }
        return null;
    }

    @Override
    public Object getData() {
        return _rawFrame;
    }
}
