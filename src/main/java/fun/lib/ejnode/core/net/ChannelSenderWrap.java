package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.callback.CbChannel;
import fun.lib.ejnode.api.callback.CbChannelReadFull;
import fun.lib.ejnode.api.callback.CbChannelResult;
import fun.lib.ejnode.api.net.ChannelSender;
import fun.lib.ejnode.core.EJNetWrap;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ChannelSenderWrap implements ChannelSender {

    public CbChannelReadFull cbRead;
    public CbChannel cbClose;
    public CbChannel cbConnIn;
    public CbChannelResult cbError;
    public CbChannel cbHandshakeDone;

    public long dstWorkerId;
    public Object userData;

    private final EJNetWrap _netWrap;
    public final TcpChannelWrap channelWrap;

    private final AtomicBoolean _sendCalled = new AtomicBoolean(false);

    public ChannelSenderWrap(EJNetWrap netWrap, TcpChannelWrap channelWrap){
        _netWrap = netWrap;
        this.channelWrap = channelWrap;
    }

    @Override
    public ChannelSender onRead(CbChannelReadFull cbRead) {
        this.cbRead = cbRead;
        return this;
    }

    @Override
    public ChannelSender onClose(CbChannel cbClose) {
        this.cbClose = cbClose;
        return this;
    }

    @Override
    public ChannelSender onConnIn(CbChannel cbConnIn) {
        this.cbConnIn = cbConnIn;
        return this;
    }

    @Override
    public ChannelSender onError(CbChannelResult cbError) {
        this.cbError = cbError;
        return this;
    }

    @Override
    public ChannelSender onHandshakeDone(CbChannel cbHandshakeDone) {
        this.cbHandshakeDone = cbHandshakeDone;
        return this;
    }

    @Override
    public ChannelSender userData(Object userData) {
        this.userData = userData;
        return this;
    }

    @Override
    public boolean send(long dstWorkerId) throws StatusIllegalException {
        if(_sendCalled.getAndSet(true)){
            throw new StatusIllegalException("channel can be send only once");
        }
        if(cbRead == null && cbClose == null && cbConnIn == null){  // no cb has set
            throw new StatusIllegalException("both cbRead and cbClose has not set");
        }
        this.dstWorkerId = dstWorkerId;
        return _netWrap.sendChannel(this);
    }
}
