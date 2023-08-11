package fun.lib.ejnode.api.net;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.callback.CbChannelReadSimple;
import fun.lib.ejnode.api.callback.CbCommon;
import fun.lib.ejnode.api.callback.CbCommonResult;

public abstract class TcpChannel {

    public abstract TcpChannel onHandshakeDone(CbCommon cb) throws StatusIllegalException;

    public abstract TcpChannel onRead(CbChannelReadSimple cb) throws StatusIllegalException;

    public abstract TcpChannel onClose(CbCommon cb) throws StatusIllegalException;

    public abstract TcpChannel onError(CbCommonResult cb) throws StatusIllegalException;

    public abstract void close();

    public abstract boolean write(Object data);

    public abstract boolean write(Object data, CbCommonResult cb);

    public abstract boolean writeThenClose(Object data);

    public abstract boolean writeToBuffer(Object data);
    public abstract boolean flushBuffer();

    public abstract boolean isWritable();

    public abstract ChannelSender sender();
}
