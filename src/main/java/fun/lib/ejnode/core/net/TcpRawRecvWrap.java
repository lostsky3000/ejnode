package fun.lib.ejnode.core.net;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.net.TcpRawRecv;
import io.netty.buffer.ByteBuf;

public class TcpRawRecvWrap extends TcpRawRecv implements IoDataInWrap {

    private final ByteBuf _buf;

    public TcpRawRecvWrap(ByteBuf buf){
        _buf = buf;
    }

    @Override
    public Object getData() {
        return _buf;
    }

    @Override
    public ByteBuf rawBuf() {
        return _buf;
    }

}
