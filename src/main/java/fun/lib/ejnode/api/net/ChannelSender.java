package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.callback.CbChannel;
import fun.lib.ejnode.api.callback.CbChannelReadFull;
import fun.lib.ejnode.api.callback.CbChannelResult;

public interface ChannelSender {

    ChannelSender onRead(CbChannelReadFull cbRead);

    ChannelSender onClose(CbChannel cbClose);

    ChannelSender onConnIn(CbChannel cbConnIn);

    ChannelSender onError(CbChannelResult cbError);

    ChannelSender onHandshakeDone(CbChannel cbHandshakeDone);

    ChannelSender userData(Object userData);

    boolean send(long dstWorkerId) throws StatusIllegalException;
}
