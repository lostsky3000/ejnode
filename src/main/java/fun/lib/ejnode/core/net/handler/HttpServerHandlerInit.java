package fun.lib.ejnode.core.net.handler;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.HttpReqRecvWrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

public final class HttpServerHandlerInit extends ChannelInitializer<SocketChannel> {

    private final CbChannelHandlerInit _cbHandlerInit;
    private final EJNetWrap _netWrapCaller;
    private final long _callerId;
    private final int _maxContentLen;

    public HttpServerHandlerInit(CbChannelHandlerInit cbHandlerInit, EJNetWrap netWrapCaller, long callerId, int maxContentLen){
        _cbHandlerInit = cbHandlerInit;
        _netWrapCaller = netWrapCaller;
        _callerId = callerId;
        _maxContentLen = maxContentLen;
    }
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipe = ch.pipeline();
        if(_cbHandlerInit != null){
            _cbHandlerInit.onHandlerInit(ch, pipe);
        }
        pipe.addLast(new HttpServerCodec());
        pipe.addLast(new HttpObjectAggregator(_maxContentLen));
        pipe.addLast(new HttpHandler(_netWrapCaller, _callerId));
    }

    static class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
        private IoProxy _proxy;
        private NodeContext _nodeCtxReader;

        private HttpHandler(EJNetWrap netWrapCaller, long callerId){
            _proxy = new IoProxy(netWrapCaller, callerId, true);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
//            doLog("channelActive 111");
            _proxy.onChannelActive(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
            if(!req.decoderResult().isSuccess()){   // decode failed
                _proxy.onChannelError(ctx, req.decoderResult().cause().toString());
                ctx.close();
                return;
            }
            //
            if(_nodeCtxReader == null){
                _proxy.checkAutoRead(ctx);
                _nodeCtxReader = _proxy.nodeCtxReader();
            }
            HttpReqRecvWrap reqWrap = new HttpReqRecvWrap(req);
            ReferenceCountUtil.retain(req);
            boolean sendSucc = _proxy.onChannelRead(ctx, reqWrap, true);
            if(!sendSucc){
                ReferenceCountUtil.release(req);
            }

//            FullHttpResponse rsp = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK);
//            // Tell the client we're going to close the connection.
//            rsp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
//            ChannelFuture f = ctx.writeAndFlush(rsp);
//            f.addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//            doLog("channelInactive 111");
            _proxy.onChannelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            doLog("exceptionCaught 111");
            cause.printStackTrace();
            _proxy.onChannelError(ctx, cause.toString());
            ctx.close();
        }

        static void doLog(Object msg){
            System.out.println(msg);
        }

    }
}
