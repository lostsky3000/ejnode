package fun.lib.ejnode.core.net;


import fun.lib.ejnode.api.net.*;

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

    public static CodecMysqlClient mysql(){
        CodecMysqlClientWrap wrap = new CodecMysqlClientWrap();
        return wrap;
    }
}
