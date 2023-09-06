package fun.lib.ejnode.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.xml.soap.Text;

public class NettyTest {


    public static void main(String[] args){
        doLog("aaaaaa");

        serverTest1();

    }

    static void clientTest1(){

    }

    static void serverTest1(){
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(2);

//        WriteBufferWaterMark writeWaterMark = new WriteBufferWaterMark(32*1000, 64*1000);

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_BACKLOG, 128)

                .channel(NioServerSocketChannel.class)
                //.channel(EpollServerSocketChannel.class)
                //
//                .childOption(ChannelOption.AUTO_READ, false)
//                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeWaterMark)

                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 2048)
                .childOption(ChannelOption.SO_RCVBUF, 2048)
//                .childHandler(new TestSocketInitializer());
                .childHandler(new TestWsInit());
        ChannelFuture future = b.bind(10086);
//        ChannelFuture future = b.bind("127.0.0.1", 10086);
        future.addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> f) throws Exception {
                boolean isDone = f.isDone();
                boolean isSucc = f.isSuccess();
                if(isDone && isSucc){
                    doLog("listen succ");
                }else{
                    doLog("listen failed: "+f.cause().getMessage());
                }
                doLog("ch1: " + future.channel().id()+", "+future.channel().isOpen()+", thread="+Thread.currentThread().getName());
//                future.channel().close();
            }
        });
        doLog("ch2: " + future.channel().id()+", "+future.channel().isActive()+", thread="+Thread.currentThread().getName());
    }

    static class TestWsInit extends ChannelInitializer<SocketChannel>{
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new WsValidateHandler("/ws"));
            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
            pipeline.addLast(new WsHandler());
        }
    }
    static class WsValidateHandler extends ChannelInboundHandlerAdapter{
        private final String _uri;
        public WsValidateHandler(String uri){
            _uri = uri;
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            FullHttpRequest req = (FullHttpRequest) msg;
            if(!req.decoderResult().isSuccess()){   // bad request
                _echoResponse(ctx, new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.BAD_REQUEST));
                ReferenceCountUtil.release(msg);
                return;
            }
            if(req.method() != HttpMethod.GET){  // only GET allowed
                _echoResponse(ctx, new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.FORBIDDEN));
                ReferenceCountUtil.release(msg);
                return;
            }
            String uri = req.uri();
            if(_uri.equals(uri)){
                //
                ctx.fireChannelRead(req);

//                ctx.channel().config().setAutoRead(false); ////
            }else{  // uri not match
                _echoResponse(ctx, new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.NOT_FOUND));
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        private void _echoResponse(ChannelHandlerContext ctx, FullHttpResponse rsp){
            ctx.writeAndFlush(rsp).addListener(ChannelFutureListener.CLOSE);
        }
    }
    static class WsHandler extends SimpleChannelInboundHandler<WebSocketFrame>{
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            doLog("channelActive 111");
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
            doLog("channelRead0, "+msg);

            TextWebSocketFrame rsp = new TextWebSocketFrame();
            rsp.content().writeCharSequence("echo from server", CharsetUtil.UTF_8);
            ctx.writeAndFlush(rsp);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            doLog("channelInactive 111");
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
                ctx.pipeline().remove(WsValidateHandler.class);
                //
                doLog("ws handshake done");
            }else{
                super.userEventTriggered(ctx, evt);
            }
        }
    }


    static class TestSocketInitializer extends ChannelInitializer<SocketChannel>{
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast("aaaa", new HandlerTest1());
            pipeline.addLast("cccc", new HandlerTest2());
        }
    }
    static class HandlerTest1 extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            ctx.channel().config().setAutoRead(true);
            ctx.channel().read();
            super.channelActive(ctx);

//            Bootstrap b = new Bootstrap();
//            b.group(ctx.channel().eventLoop())
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //super.channelRead(ctx, msg);

            ReferenceCountUtil.release(msg);
            ctx.fireChannelRead("hehe");
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            super.channelReadComplete(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }
    }

    static class HandlerTest2 extends SimpleChannelInboundHandler<String>{

        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
            int n = 1;
        }

//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            int n = 1;
//        }
    }

    static void doLog(Object msg){
        System.out.println(msg);
    }
}
