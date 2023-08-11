package fun.lib.ejnode.core.net.handler;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.TcpRawRecvWrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;

public class RawHandlerInit extends ChannelInitializer<SocketChannel> {

    private final long _callerId;
    private final EJNetWrap _netWrapCaller;
    private final CbChannelHandlerInit _cbHandlerInit;
    private final boolean _isServer;

    public RawHandlerInit(CbChannelHandlerInit cbHandlerInit, EJNetWrap netWrapCaller, long callerId, boolean isServer){
        _cbHandlerInit = cbHandlerInit;
        _netWrapCaller = netWrapCaller;
        _callerId = callerId;
        _isServer = isServer;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();
        if(_cbHandlerInit != null){
            _cbHandlerInit.onHandlerInit(ch, pipe);
        }
        pipe.addLast(new RawHandler(_netWrapCaller, _callerId, _isServer));
    }

    static class RawHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private IoProxy _proxy;
        private NodeContext _nodeCtxReader;

        private RawHandler(EJNetWrap netWrapCaller, long callerId, boolean isServer){
            _proxy = new IoProxy(netWrapCaller, callerId, isServer);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            _proxy.onChannelActive(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            if(_nodeCtxReader == null){
                _proxy.checkAutoRead(ctx);
                _nodeCtxReader = _proxy.nodeCtxReader();
            }
            TcpRawRecvWrap wrap = new TcpRawRecvWrap(msg);
            ReferenceCountUtil.retain(msg);
            boolean sendSucc = _proxy.onChannelRead(ctx, wrap, true);
            if(!sendSucc){
                ReferenceCountUtil.release(msg);
            }

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            _proxy.onChannelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            _proxy.onChannelError(ctx, cause.toString());
            ctx.close();
        }
    }
}
