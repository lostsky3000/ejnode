package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.callback.CbChannel;
import fun.lib.ejnode.api.callback.CbServerResult;
import fun.lib.ejnode.api.net.*;
import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.handler.CbChannelHandlerInit;
import fun.lib.ejnode.core.net.handler.HttpServerHandlerInit;
import fun.lib.ejnode.core.net.handler.RawHandlerInit;
import fun.lib.ejnode.core.net.handler.WsServerHandlerInit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public final class TcpServerWrap implements TcpServer, TcpServerFuture, TcpServerChannel, CbChannelHandlerInit {

    public final long id;
    protected final int port;
    protected String host;

    private int _codecType;

    private SockCfgSvrWrap _soCfg;

    private Codec4Server _codec;
    private EJNetWrap _netWrap;

    private CbServerResult _cbResult;
    private CbChannel _cbAccept;

    private Channel _channel;

    private boolean _closeCalled;

    public TcpServerWrap(long channelId, String host, int port, EJNetWrap netWrap){
        id = channelId;
        _initDefault();
        this.port = port;
        if(host != null){
            this.host = host;
        }
        _netWrap = netWrap;
    }
    public int codecType(){
        return _codecType;
    }

    @Override
    public TcpServer codec(Codec4Server codec) {
        _codec = codec;
        return this;
    }

    @Override
    public SockCfgServer<TcpServer> soBegin() {
        _soCfg = new SockCfgSvrWrap(this);
        return _soCfg;
    }

    @Override
    public TcpServerFuture listen() {
        return listen(null);
    }

    @Override
    public TcpServerFuture listen(CbServerResult cb) {
        _cbResult = cb;
        _netWrap.onTcpServerStart(this);
        String err = _doTcpServer();
        if(err != null){ // has error
            _netWrap.checkListenResult(err, null, id);
        }
        return this;
    }

    @Override
    public TcpServerFuture onAccept(CbChannel cb) {
        _cbAccept = cb;
        return this;
    }
    public boolean hasSetCbAccept(){
        return _cbAccept != null;
    }

    public void onListenResult(String error, Channel channel){
        _channel = channel;
        boolean isSucc = channel!=null;
        if(_closeCalled){   // close() has be called, no need notify, close channel
            if(isSucc){
                _netWrap.onTcpServerClose(this);
                channel.close();
                _channel = null;
            }
        }else{
            if(_cbResult != null){
                _cbResult.onResult(error, isSucc?this:null);
            }
        }
    }

    public void onChannelAccept(TcpChannelWrap channelWrap){
        if(_cbAccept != null){
            _cbAccept.onCallback(channelWrap);
        }
    }

    private void _initDefault(){
        host = "0.0.0.0";
    }

    private String _doTcpServer(){
        String err = null;
        do{
            if(_soCfg == null){  // no sockCfg, use default
                _soCfg = new SockCfgSvrWrap(this);
            }
            if(_codec == null){
                _codec = ServerCodec.bytes();
            }
            EventLoopGroup grpBoss = _netWrap.getIoMgr().ioGroupBoss();
            EventLoopGroup grpWorkerOutter = _netWrap.getIoMgr().ioGroupWorkerOutter();
            //
            ServerBootstrap b = new ServerBootstrap();
            b.group(grpBoss, grpWorkerOutter)
                    .channel((grpBoss instanceof EpollEventLoopGroup)?EpollServerSocketChannel.class:NioServerSocketChannel.class)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_BACKLOG, _soCfg.backLog)
                    //
//                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeWaterMark)
                    .childOption(ChannelOption.AUTO_READ, false)
                    //
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, _soCfg.keepAlive)
                    .childOption(ChannelOption.TCP_NODELAY, _soCfg.tcpNoDelay)
                    .childOption(ChannelOption.SO_SNDBUF, _soCfg.sendBuff)
                    .childOption(ChannelOption.SO_RCVBUF, _soCfg.recvBuff)
                    .childHandler(_createChannelInit(_codec));
//            if(_codec.codecType() == NetCodec.TYPE_WEBSOCKET_SERVER){
//                b.childOption(ChannelOption.AUTO_READ, true);
//            }else{
//                b.childOption(ChannelOption.AUTO_READ, false);
//            }
            try{
                final ChannelFuture f = b.bind(host,port);
                f.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        String err = null;
                        if(future.isSuccess()){
//                            _netWrap.getLogger().logInfo("listen succ");
                        }else{
//                            _netWrap.getLogger().logErr("listen failed");
                            err = future.cause().getMessage();
                            f.channel().close();
                        }
                        _netWrap.checkListenResult(err, f.channel(), id);
                    }
                });
            }catch (Throwable e){
                err = e.getMessage();
            }
        }while (false);
        return err;
    }

    private ChannelInitializer<SocketChannel> _createChannelInit(Codec4Server codec){
        int codecType = codec.codecType();
        _codecType = codecType;
        if(codecType == NetCodec.TYPE_WEBSOCKET_SERVER){
            CodecWebsocketServerWrap cfg = (CodecWebsocketServerWrap) codec;
            WsServerHandlerInit init = new WsServerHandlerInit(this, _netWrap, id, cfg.uri, cfg.contentMaxLen);
            return init;
        }else if(codecType == NetCodec.TYPE_HTTP_SERVER){
            CodecHttpServerWrap cfg = (CodecHttpServerWrap) codec;
            HttpServerHandlerInit init = new HttpServerHandlerInit(this, _netWrap, id, cfg.reqMaxBytes);
            return init;
        }else if(codecType == NetCodec.TYPE_RAW_SERVER){
            RawHandlerInit init = new RawHandlerInit(this, _netWrap, id, true);
            return init;
        }
        return null;
    }

    //TcpServerChannel
    @Override
    public void close() {
        if(_closeCalled){
            return;
        }
        _closeCalled = true;
        if(_channel != null){
            _netWrap.onTcpServerClose(this);
            _channel.close();
            _channel = null;
        }
    }

    @Override
    public void onHandlerInit(SocketChannel sockChannel, ChannelPipeline pipeline) {

    }
}
