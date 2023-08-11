package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.CodecRawServer;

public final class CodecRawServerWrap implements CodecRawServer {
    @Override
    public int codecType() {
        return TYPE_RAW_SERVER;
    }
}
