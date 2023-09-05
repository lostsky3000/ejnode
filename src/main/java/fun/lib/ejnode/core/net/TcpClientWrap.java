package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.callback.CbChannelResult;
import fun.lib.ejnode.api.net.*;
import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.net.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;


public final class TcpClientWrap implements TcpClient, CbChannelHandlerInit {

    public final long id;
    private final String _host;
    private final int _port;
    private final EJNetWrap _netWrap;
    //
    private SockCfgCliWrap _soCfg;
    private volatile SslCfgCliWrap _sslCfg;
    private Codec4Client _codec;
    private long _timeoutMs;
    private CbChannelResult _cbResult;
    private int _codecType;

    private boolean _connCalled;

    public TcpClientWrap(long id, String host, int port, EJNetWrap netWrap){
        this.id = id;
        _host = host;
        _port = port;
        _netWrap = netWrap;
        _connCalled = false;
        //
        _initDefault();
    }

    @Override
    public SockCfgClient<TcpClient> soBegin() {
        _soCfg = new SockCfgCliWrap(this);
        return _soCfg;
    }

    @Override
    public SslCfgClient<TcpClient> sslBegin() {
        _sslCfg = new SslCfgCliWrap(this);
        return _sslCfg;
    }

    @Override
    public TcpClient codec(Codec4Client codec) {
        _codec = codec;
        return this;
    }

    public int codecType(){
        return _codecType;
    }

    @Override
    public TcpClient timeout(long timeoutMs) {
        _timeoutMs = timeoutMs;
        return this;
    }

    public void onConnResult(String error, TcpChannelWrap channelWrap){
        _cbResult.onCallback(error, channelWrap);
    }

    @Override
    public void connect(CbChannelResult cb) {
        if(_connCalled || cb == null){
            return;
        }
        _connCalled = true;
        _cbResult = cb;
        //
        _netWrap.onTcpClientStart(this);
        String error = _doConnect();
        if(error != null){
            _netWrap.onConnectFailed(error, id);
        }
    }

    private String _doConnect(){
        String err = null;
        do{
            if(_soCfg == null){  // no sockCfg, use default
                _soCfg = new SockCfgCliWrap(this);
            }
            if(_codec == null){  // no codec, use default
                _codec = ClientCodec.bytes();
            }
            EventLoopGroup group = _netWrap.getIoMgr().ioGroupWorkerInner();
            Bootstrap boot = new Bootstrap();
            boot.group(group)
                    .channel((group instanceof EpollEventLoopGroup)? EpollSocketChannel.class: NioSocketChannel.class)
                    .option(ChannelOption.ALLOCATOR,
                            PooledByteBufAllocator.DEFAULT)

//                    .option(ChannelOption.AUTO_READ, _codec.codecType()==NetCodec.TYPE_WEBSOCKET_CLIENT)
                    .option(ChannelOption.AUTO_READ, false)

                    .option(ChannelOption.SO_KEEPALIVE, _soCfg.keepAlive)
                    .option(ChannelOption.SO_RCVBUF, _soCfg.recvBuff)
                    .option(ChannelOption.SO_SNDBUF, _soCfg.sendBuff)
                    .option(ChannelOption.TCP_NODELAY, _soCfg.tcpNoDelay)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int)_timeoutMs)
                    .handler(_createChannelInit(_codec));
            try{
                final ChannelFuture future = boot.connect(_host, _port);
                future.addListener(future1 -> {
                    if(future1.isSuccess()){  // conn succ
                        // ChannelHandler will notify worker
                    }else{  // conn failed
                        String error = future1.cause().toString();
                        // notify
                        _netWrap.onConnectFailed(error, id);
                    }
                });
            }catch (Throwable e){
//                e.printStackTrace();
                err = e.getMessage();
                break;
            }
        }while (false);
        return err;
    }

    private ChannelInitializer<SocketChannel> _createChannelInit(NetCodec codec){
        int codecType = codec.codecType();
        _codecType = codecType;
        if(codecType == NetCodec.TYPE_HTTP_CLIENT){
            CodecHttpClientWrap cfg = (CodecHttpClientWrap) codec;
            HttpClientHandlerInit init = new HttpClientHandlerInit(this, _netWrap, id, cfg.contentMaxLen);
            return init;
        }
        else if(codecType == NetCodec.TYPE_WEBSOCKET_CLIENT){
            CodecWebsocketClientWrap cfg = (CodecWebsocketClientWrap) codec;
            WsClientHandlerInit init = new WsClientHandlerInit(this, _netWrap, id, cfg.contentMaxLen, cfg.uri);
            return init;
        }else if(codecType == NetCodec.TYPE_REDIS_CLIENT){
            CodecRedisClientWrap cfg = (CodecRedisClientWrap) codec;
            RedisClientHandlerInit init = new RedisClientHandlerInit(this, _netWrap, id, cfg.cacheMax);
            return init;
        }
        else if(codecType == NetCodec.TYPE_RAW_CLIENT){
            RawHandlerInit init = new RawHandlerInit(this, _netWrap, id, false);
            return init;
        }
        else if(codecType == NetCodec.TYPE_MYSQL_CLIENT){
            CodecMysqlClientWrap cfg = (CodecMysqlClientWrap) codec;
            MysqlClientHandlerInit init = new MysqlClientHandlerInit(this, _netWrap, id);
            return init;
        }
        return null;
    }

    private void _initDefault(){
        _timeoutMs = 10000;
    }

    @Override
    public void onHandlerInit(SocketChannel sockChannel, ChannelPipeline pipeline) {
        if(_sslCfg != null){
            try {
                SslContext sslCtx = SslContextBuilder.forClient()
//                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                pipeline.addLast(sslCtx.newHandler(sockChannel.alloc()));
            } catch (SSLException e) {
                e.printStackTrace();
            }
        }
    }
}
