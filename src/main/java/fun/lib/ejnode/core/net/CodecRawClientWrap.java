package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.CodecRawClient;

public final class CodecRawClientWrap implements CodecRawClient {

    @Override
    public int codecType() {
        return TYPE_RAW_CLIENT;
    }
}
