package fun.lib.ejnode.core.net;


import fun.lib.ejnode.api.net.CodecHttpClient;
import fun.lib.ejnode.api.net.CodecRawClient;
import fun.lib.ejnode.api.net.CodecRedisClient;
import fun.lib.ejnode.api.net.CodecWebsocketClient;

public final class ClientCodec {
    public static CodecHttpClient http(){
        CodecHttpClientWrap wrap = new CodecHttpClientWrap();
        return wrap;
    }

    public static CodecWebsocketClient websocket(){
        CodecWebsocketClientWrap wrap = new CodecWebsocketClientWrap();
        return wrap;
    }

    public static CodecRawClient bytes(){
        CodecRawClientWrap wrap = new CodecRawClientWrap();
        return wrap;
    }

    public static CodecRedisClient redis(){
        CodecRedisClientWrap wrap = new CodecRedisClientWrap();
        return wrap;
    }
}
