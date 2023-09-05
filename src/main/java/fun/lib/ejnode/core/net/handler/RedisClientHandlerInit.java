package fun.lib.ejnode.core.net.handler;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.RedisRecvWrap;
import fun.lib.ejnode.core.db.redis.RedisDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

public final class RedisClientHandlerInit extends ChannelInitializer<SocketChannel> {

    private final long _callerId;
    private final EJNetWrap _netWrapCaller;
    private final CbChannelHandlerInit _cbHandlerInit;

    private final int _cacheMax;

    public RedisClientHandlerInit(CbChannelHandlerInit cbHandlerInit, EJNetWrap netWrapCaller, long callerId, int cacheMax){
        _cbHandlerInit = cbHandlerInit;
        _netWrapCaller = netWrapCaller;
        _callerId = callerId;
        _cacheMax = cacheMax;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();
        if(_cbHandlerInit != null){
            _cbHandlerInit.onHandlerInit(ch, pipe);
        }
        pipe.addLast(new RedisHandler(_netWrapCaller, _callerId, _cacheMax));
    }

    static class RedisHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private IoProxy _proxy;
        private NodeContext _nodeCtxReader;
        private RedisDecoder _decoder;
        private int _cacheMax;

        private RedisHandler(EJNetWrap netWrapCaller, long callerId, int cacheMax){
            _proxy = new IoProxy(netWrapCaller, callerId, false);
            _cacheMax = cacheMax;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ByteBuf bufCache = PooledByteBufAllocator.DEFAULT.buffer(1024 * 4);
            _decoder = new RedisDecoder(bufCache, _cacheMax);
            //
            _proxy.onChannelActive(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            try {
                _decoder.onRecv(msg);
            }catch (Throwable e){     // parse error(cache overflow)
                e.printStackTrace();
                ctx.close();
                _proxy.onChannelError(ctx, e.toString());
                return;
            }
//            if(_nodeCtxReader == null){
//                _proxy.checkAutoRead(ctx);
//                _nodeCtxReader = _proxy.nodeCtxReader();
//            }

            while(_decoder.resultNum() > 0){
                Object ret = _decoder.popResult();
//                RedisRecvWrap wrap = new RedisRecvWrap(ret);
                _proxy.onChannelRead(ctx, ret, false);
            }

//            TcpRawRecvWrap wrap = new TcpRawRecvWrap(msg);
//            wrap.setNodeCtx(_nodeCtxReader);
//            ReferenceCountUtil.retain(msg);
//            boolean sendSucc = _proxy.onChannelRead(ctx, wrap, true);
//            if(!sendSucc){
//                ReferenceCountUtil.release(msg);
//            }

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            _decoder.onRelease();
            _proxy.onChannelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            _proxy.onChannelError(ctx, cause.toString());
            ctx.close();
        }
    }
}
