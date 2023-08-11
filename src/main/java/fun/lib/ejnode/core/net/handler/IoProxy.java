package fun.lib.ejnode.core.net.handler;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.IoHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public final class IoProxy implements IoHandler {

    private final boolean _isServer;
    private boolean _autoRead;
    private EJNetWrap _netWrapCaller;
    private long _callerId;

    private EJNetWrap _netWrapRead;
    private volatile EJNetWrap _netWrapReadVolatile;
    private volatile Channel _channel;
    private long _channelId;
    private volatile long _channelIdVolatile;

    private boolean _activeCalled;

    protected IoProxy(EJNetWrap netWrapCaller, long callerId, boolean isServer){
        _netWrapCaller = netWrapCaller;
        _callerId = callerId;
        _isServer = isServer;
        _activeCalled = false;
    }
    protected void onChannelActive(Channel channel){
        if(_activeCalled){
            return;
        }
        _activeCalled = true;
        _autoRead = false;
        _netWrapRead = null;
        _channel = channel;
        _netWrapCaller.onChannelActive(channel, _callerId, _isServer, this);
    }
    protected boolean onChannelRead(ChannelHandlerContext ctx, Object msg, boolean needRelease){
        checkAutoRead(ctx);
        // ensure _netWrapRead is not null
        return _netWrapRead.onChannelRead(_channelId, msg, needRelease);
    }

    protected void onChannelInactive(ChannelHandlerContext ctx){
        checkAutoRead(ctx);
        if(_netWrapRead != null){
            _netWrapRead.onChannelInactive(_channelId);
        }
    }

    protected void onChannelError(ChannelHandlerContext ctx, String error){
        checkAutoRead(ctx);
        if(_netWrapRead != null){
            _netWrapRead.onChannelError(_channelId, error);
        }
    }

    protected void onHandshakeDone(ChannelHandlerContext ctx){
        checkAutoRead(ctx);
        if(_netWrapRead != null){
            _netWrapRead.onChannelHandshakeDone(_channelId);
        }
    }

    protected void checkAutoRead(ChannelHandlerContext ctx){
        if(!_autoRead){
            _autoRead = true;
            _netWrapRead = _netWrapReadVolatile;
            _channelId = _channelIdVolatile;
        }
    }

    protected NodeContext nodeCtxReader(){
        if(_netWrapRead != null){
            return  _netWrapRead.nodeCtx();
        }
        return null;
    }

    @Override
    public Channel getChannel() {
        return _channel;
    }

    @Override
    public void onReadStart(EJNetWrap netWrap, long channelId) {
        _netWrapReadVolatile = netWrap;
        _channelIdVolatile = channelId;
        _channel.config().setAutoRead(true);
    }
}
