package fun.lib.ejnode.api.net;

import fun.lib.ejnode.core.NodeContext;
import io.netty.buffer.ByteBuf;

public abstract class TcpRawRecv {

    public abstract ByteBuf rawBuf();

}
