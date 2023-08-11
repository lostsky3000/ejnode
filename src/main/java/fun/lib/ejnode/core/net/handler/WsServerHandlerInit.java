package fun.lib.ejnode.core.net.handler;

import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.WsFrameWrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.ReferenceCountUtil;

public final class WsServerHandlerInit extends ChannelInitializer<SocketChannel> {

    private final String _wsUri;
    private final int _contentMaxLen;

    private final EJNetWrap _netWrapCaller;
    private final long _callerId;

    private final CbChannelHandlerInit _cbHandlerInit;

    public WsServerHandlerInit(CbChannelHandlerInit cbHandlerInit, EJNetWrap netWrapCaller, long callerId, String wsUri, int contentMaxLen){
        _cbHandlerInit = cbHandlerInit;
        _wsUri = wsUri;
        _contentMaxLen = contentMaxLen;
        _netWrapCaller = netWrapCaller;
        _callerId = callerId;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();
        if(_cbHandlerInit != null){
            _cbHandlerInit.onHandlerInit(ch, pipe);
        }

        IoProxy proxy = new IoProxy(_netWrapCaller, _callerId, true);

        pipe.addLast(new HttpServerCodec());
        pipe.addLast(new HttpObjectAggregator(_contentMaxLen));
        pipe.addLast(new WsSvrValidateHandler(_wsUri, proxy));
//        pipe.addLast(new WebSocketServerCompressionHandler());
        pipe.addLast(new WebSocketServerProtocolHandler(_wsUri));
        pipe.addLast(new WsHandler(proxy));
    }

    static class WsSvrValidateHandler extends ChannelInboundHandlerAdapter{
        private final String _uri;
        private final IoProxy _proxy;

        public WsSvrValidateHandler(String uri, IoProxy proxy){
            _uri = uri;
            _proxy = proxy;
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            FullHttpRequest req = (FullHttpRequest) msg;
            if(!req.decoderResult().isSuccess()){  // bad request
                _echoResponse(ctx, new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.BAD_REQUEST));
                ReferenceCountUtil.release(req);
                // notify
                _proxy.onChannelError(ctx, req.decoderResult().cause().toString());
                return;
            }
            if(req.method() != HttpMethod.GET){
                _echoResponse(ctx, new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.FORBIDDEN));
                ReferenceCountUtil.release(req);
                // notify
                _proxy.onChannelError(ctx, "invalid method: " + req.method());
                return;
            }
            if(_uri.equals(req.uri())){  // uri match
                ctx.fireChannelRead(req);
            }else{
                _echoResponse(ctx, new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.NOT_FOUND));
                ReferenceCountUtil.release(req);
                // notify
                _proxy.onChannelError(ctx, "uri not match: "+req.uri());
                return;
            }
        }

        private void _echoResponse(ChannelHandlerContext ctx, FullHttpResponse rsp){
            ctx.writeAndFlush(rsp).addListener(ChannelFutureListener.CLOSE);
        }
    }

    static class WsHandler extends SimpleChannelInboundHandler<WebSocketFrame>{
        private IoProxy _proxy;

        public WsHandler(IoProxy proxy){
            _proxy = proxy;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            doLog("WsHandler, channelActive");
            _proxy.onChannelActive(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

            WsFrameWrap frameWrap = null;
            if(msg instanceof TextWebSocketFrame){
                frameWrap = new WsFrameWrap(msg, true);
            }else if(msg instanceof BinaryWebSocketFrame){
                frameWrap = new WsFrameWrap(msg, false);
            }else if(msg instanceof PingWebSocketFrame){
                ctx.write(new PongWebSocketFrame());
            }else if(msg instanceof CloseWebSocketFrame){
                ctx.close();
            }
            if(frameWrap != null){
                ReferenceCountUtil.retain(msg);
                if(!_proxy.onChannelRead(ctx, frameWrap, true)){
                    ReferenceCountUtil.release(msg);
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            doLog("WsHandler, channelInactive");
            _proxy.onChannelInactive(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
                ctx.pipeline().remove(WsSvrValidateHandler.class);
                // notify handshake done
                _proxy.onHandshakeDone(ctx);
            }else {
                super.userEventTriggered(ctx, evt);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            _proxy.onChannelError(ctx, cause.toString());
            ctx.close();
        }
    }

    static void doLog(Object msg){

//        System.out.println(msg);
    }
}
