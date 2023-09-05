package fun.lib.ejnode.core.net.handler;

import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.db.mysql.MysqlDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

public final class MysqlClientHandlerInit extends ChannelInitializer<SocketChannel> {

    private final long _callerId;
    private final EJNetWrap _netWrapCaller;
    private final CbChannelHandlerInit _cbHandlerInit;

    public MysqlClientHandlerInit(CbChannelHandlerInit cbHandlerInit, EJNetWrap netWrapCaller, long callerId){
        _cbHandlerInit = cbHandlerInit;
        _netWrapCaller = netWrapCaller;
        _callerId = callerId;
        //
//        _cacheMax = cacheMax;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();
        if(_cbHandlerInit != null){
            _cbHandlerInit.onHandlerInit(ch, pipe);
        }
        pipe.addLast(new MysqlHandler(_netWrapCaller, _callerId));
    }

    static class MysqlHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private IoProxy _proxy;
        private NodeContext _nodeCtxReader;
        private MysqlDecoder _decoder;

//        private RedisDecoder _decoder;
//        private int _cacheMax;

        private MysqlHandler(EJNetWrap netWrapCaller, long callerId){
            _proxy = new IoProxy(netWrapCaller, callerId, false);
//            _cacheMax = cacheMax;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            ByteBuf bufCache = PooledByteBufAllocator.DEFAULT.buffer(1024 * 4);
//            _decoder = new RedisDecoder(bufCache, _cacheMax);
            _decoder = new MysqlDecoder();
            //
            _proxy.onChannelActive(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            try {
                _decoder.onRecv(msg);
            }catch (Throwable e){
                e.printStackTrace();
                ctx.close();
            }
//            try {
//                _decoder.onRecv(msg);
//            }catch (Throwable e){     // parse error(cache overflow)
//                e.printStackTrace();
//                ctx.close();
//                _proxy.onChannelError(ctx, e.toString());
//                return;
//            }
//            while(_decoder.resultNum() > 0){
//                Object ret = _decoder.popResult();
////                RedisRecvWrap wrap = new RedisRecvWrap(ret);
//                _proxy.onChannelRead(ctx, ret, false);
//            }

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//            _decoder.onRelease();
            _proxy.onChannelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            _proxy.onChannelError(ctx, cause.toString());
            ctx.close();
        }
    }
}
