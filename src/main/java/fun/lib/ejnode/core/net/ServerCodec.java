package fun.lib.ejnode.core.net;


import fun.lib.ejnode.api.net.CodecHttpServer;
import fun.lib.ejnode.api.net.CodecRawServer;
import fun.lib.ejnode.api.net.CodecWebsocketServer;

public final class ServerCodec {

    public static CodecHttpServer http(){
        CodecHttpServerWrap wrap = new CodecHttpServerWrap();
        return wrap;
    }

    public static CodecWebsocketServer websocket(){
        CodecWebsocketServer wrap = new CodecWebsocketServerWrap();
        return wrap;
    }

    public static CodecRawServer bytes(){
        CodecRawServerWrap wrap = new CodecRawServerWrap();
        return wrap;
    }
}
