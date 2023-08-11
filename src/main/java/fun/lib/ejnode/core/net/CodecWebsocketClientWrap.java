package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.CodecWebsocketClient;

public final class CodecWebsocketClientWrap implements CodecWebsocketClient {

    public int contentMaxLen;
    public String uri;

    public CodecWebsocketClientWrap(){
        _initDefault();
    }

    @Override
    public CodecWebsocketClient reqMaxBytes(int maxBytes) {
        contentMaxLen = maxBytes;
        return this;
    }

    @Override
    public CodecWebsocketClient uri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public int codecType() {
        return TYPE_WEBSOCKET_CLIENT;
    }

    private void _initDefault(){
        contentMaxLen = 1024 * 64;
    }
}
