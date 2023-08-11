package fun.lib.ejnode.core;

public final class MsgIO {

    public boolean needRelease;
    public int type;
    public int event;
    public long session;
    public Object userData;

    protected MsgIO(){

    }

    public static final int TYPE_LISTEN_SUCC = 1;
    public static final int TYPE_LISTEN_FAILED = 2;
    public static final int TYPE_SERVER_ACCEPT = 3;
    public static final int TYPE_MSG = 4;

    public static final int TYPE_SEND_CHANNEL = 6;

    public static final int TYPE_CONNECT_SUCC = 7;
    public static final int TYPE_CONNECT_FAILED = 8;

    public static final int TYPE_CHANNEL_EVENT = 9;


    //
    public static final int EVENT_CHANNEL_GONE = 1;
    public static final int EVENT_CHANNEL_ERROR = 2;
    public static final int EVENT_CHANNEL_HANDSHAKE_DONE = 3;
}
