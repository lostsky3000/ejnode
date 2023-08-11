package fun.lib.ejnode.core.net.handler;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.HttpRspRecvWrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.ReferenceCountUtil;

public final class HttpClientHandlerInit extends ChannelInitializer<SocketChannel> {

    private final long _callerId;
    private final EJNetWrap _netWrapCaller;
    private final CbChannelHandlerInit _cbHandlerInit;

    private final int _maxContentLen;

    public HttpClientHandlerInit(CbChannelHandlerInit cbHandlerInit, EJNetWrap netWrapCaller, long callerId, int maxContentLen){
        _cbHandlerInit = cbHandlerInit;
        _netWrapCaller = netWrapCaller;
        _callerId = callerId;
        _maxContentLen = maxContentLen;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();
        if(_cbHandlerInit != null){
            _cbHandlerInit.onHandlerInit(ch, pipe);
        }
        pipe.addLast(new HttpClientCodec());
        pipe.addLast(new HttpObjectAggregator(_maxContentLen));
        pipe.addLast(new HttpHandler(_netWrapCaller, _callerId));
    }

    static class HttpHandler extends SimpleChannelInboundHandler<FullHttpResponse>{

        private IoProxy _proxy;
        private NodeContext _nodeCtxReader;

        private HttpHandler(EJNetWrap netWrapCaller, long callerId){
            _proxy = new IoProxy(netWrapCaller, callerId, false);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            _proxy.onChannelActive(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse rsp) throws Exception {
            if(!rsp.decoderResult().isSuccess()){
                _proxy.onChannelError(ctx, rsp.decoderResult().cause().toString());
                ctx.close();
                return;
            }
            if(_nodeCtxReader == null){
                _proxy.checkAutoRead(ctx);
                _nodeCtxReader = _proxy.nodeCtxReader();
            }
            HttpRspRecvWrap rspWrap = new HttpRspRecvWrap(rsp);
            ReferenceCountUtil.retain(rsp);
            boolean sendSucc = _proxy.onChannelRead(ctx, rspWrap, true);
            if(!sendSucc){
                ReferenceCountUtil.release(rsp);
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
