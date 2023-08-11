package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.CodecWebsocketServer;

public final class CodecWebsocketServerWrap implements CodecWebsocketServer {

    protected String uri;
    protected int contentMaxLen;

    protected CodecWebsocketServerWrap(){
        _initDefault();
    }

    private void _initDefault(){
        uri = "/";
        contentMaxLen = 1024 * 64;
    }

    @Override
    public CodecWebsocketServer reqMaxBytes(int maxBytes) {
        contentMaxLen = maxBytes;
        return this;
    }

    @Override
    public CodecWebsocketServer uri(String uri) {
        if(uri != null){
            this.uri = uri.trim();
        }
        return this;
    }

    @Override
    public int codecType() {
        return TYPE_WEBSOCKET_SERVER;
    }
}
