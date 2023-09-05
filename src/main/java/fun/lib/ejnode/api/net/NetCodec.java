package fun.lib.ejnode.api.net;

public interface NetCodec {

    int codecType();


    public static final int TYPE_HTTP_SERVER = 1;

    public static final int TYPE_WEBSOCKET_SERVER = 2;

    public static final int TYPE_HTTP_CLIENT = 3;

    public static final int TYPE_WEBSOCKET_CLIENT = 4;

    public static final int TYPE_RAW_CLIENT = 5;

    public static final int TYPE_RAW_SERVER = 6;

    public static final int TYPE_REDIS_CLIENT = 7;

    public static final int TYPE_MYSQL_CLIENT = 8;
}
