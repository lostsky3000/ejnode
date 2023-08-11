package fun.lib.ejnode.core.net.handler;

import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.WsFrameWrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;

import java.net.URI;

public final class WsClientHandlerInit extends ChannelInitializer<SocketChannel> {

    private final long _callerId;
    private final EJNetWrap _netWrapCaller;
    private final int _contentMaxLen;
    private final CbChannelHandlerInit _cbHandlerInit;
    private final String _uri;

    public WsClientHandlerInit(CbChannelHandlerInit cbHandlerInit, EJNetWrap netWrapCaller, long callerId, int contentMaxLen, String uri){
        _cbHandlerInit = cbHandlerInit;
        _netWrapCaller = netWrapCaller;
        _callerId = callerId;
        _contentMaxLen = contentMaxLen;
        _uri = uri;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();
        if(_cbHandlerInit != null){
            _cbHandlerInit.onHandlerInit(ch, pipe);
        }
        pipe.addLast(new HttpClientCodec());
        pipe.addLast(new HttpObjectAggregator(_contentMaxLen));
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory
                .newHandshaker(new URI(_uri), WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
        pipe.addLast(new WsHandler(handshaker, _netWrapCaller, _callerId));
    }

    static class WsHandler extends SimpleChannelInboundHandler<Object>{
        private final WebSocketClientHandshaker _handshaker;
//        private ChannelPromise _handshakeFuture;
        private IoProxy _proxy;
        private boolean _handshakeDoneNotified;

        private WsHandler(WebSocketClientHandshaker handshaker, EJNetWrap netWrapCaller, long callerId){
            _handshaker = handshaker;
            _proxy = new IoProxy(netWrapCaller, callerId, false);
        }

//        @Override
//        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//            _handshakeFuture = ctx.newPromise();
//        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            _proxy.onChannelActive(ctx.channel());
            //
            _handshakeDoneNotified = false;
            _handshaker.handshake(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(!_handshaker.isHandshakeComplete()){  // complete handshake manually
                try {
                    _handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
//                    _handshakeFuture.setSuccess();
                    if(!_handshakeDoneNotified){
                        _handshakeDoneNotified = true;
                        _proxy.onHandshakeDone(ctx);
                    }
                }
//                catch (WebSocketHandshakeException e){
                catch (Throwable e){
                    _dbgDumpError(e);
                    _proxy.onChannelError(ctx, e.toString());
                    ctx.close();
//                    _handshakeFuture.setFailure(e);
                }
                return;
            }
            if(!_handshakeDoneNotified){
                _handshakeDoneNotified = true;
                _proxy.onHandshakeDone(ctx);
            }

            WsFrameWrap frameWrap = null;
            if(msg instanceof TextWebSocketFrame){
                frameWrap = new WsFrameWrap((WebSocketFrame)msg, true);
            }else if(msg instanceof BinaryWebSocketFrame){
                frameWrap = new WsFrameWrap((WebSocketFrame)msg, false);
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
            _proxy.onChannelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            _dbgDumpError(cause);
//            if (!_handshakeFuture.isDone()) {
//                _handshakeFuture.setFailure(cause);
//            }
            _proxy.onChannelError(ctx, cause.toString());

            ctx.close();
        }


        private void _dbgDumpError(Throwable e){
            e.printStackTrace();
        }
    }
}
