package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.CodecHttpClient;

public final class CodecHttpClientWrap implements CodecHttpClient {

    public int contentMaxLen;

    public CodecHttpClientWrap(){
        _initDefault();
    }

    @Override
    public CodecHttpClient reqMaxBytes(int maxBytes) {
        contentMaxLen = maxBytes;
        return this;
    }

    @Override
    public int codecType() {
        return TYPE_HTTP_CLIENT;
    }

    private void _initDefault(){
        contentMaxLen = 1024*64;
    }
}
