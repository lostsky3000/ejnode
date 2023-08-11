package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.CodecHttpServer;

public final class CodecHttpServerWrap implements CodecHttpServer {

    protected int reqMaxBytes;

    protected CodecHttpServerWrap(){
        _initDefault();
    }

    @Override
    public CodecHttpServer reqMaxBytes(int maxBytes) {
        reqMaxBytes = maxBytes;
        return this;
    }

    private void _initDefault(){
        reqMaxBytes = 1024 * 64;
    }

    @Override
    public int codecType() {
        return TYPE_HTTP_SERVER;
    }
}
