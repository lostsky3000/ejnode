package fun.lib.ejnode.core;

import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.api.net.*;
import fun.lib.ejnode.core.net.*;
import fun.lib.ejnode.util.container.DFIdxVerList;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class EJNetWrap extends Net {

    private LoopWorker _worker;
    private EJEntryWrap _entryWrap;
    private EJIoMgr _ioMgr;
    private EJLoggerCore _logger;
    private EJWorkerMgr _workerMgr;

    private final HashMap<Long, TcpServerWrap> _mapTcpServer = new HashMap<>();
    private long _tcpServerIdCnt;

    private final HashMap<Long, TcpClientWrap> _mapTcpClient = new HashMap<>();
    private long _tcpClientIdCnt;

    private final DFIdxVerList<TcpChannelWrap> _lsOnReadChannel = new DFIdxVerList<>();
    private final HashMap<Integer, TcpChannelWrap> _mapTempChannel = new HashMap<>();
    private static AtomicInteger s_channelIdCnt = new AtomicInteger(0);

    private HttpHelperWrap _httpHelperWrap;
    private WsHelperWrap _wsHelperWrap;
    private NodeContext _nodeCtx;
    private EJTimerWrap _timerWrap;

    protected EJNetWrap(LoopWorker worker, EJEntryWrap entryWrap){
        _worker = worker;
        _entryWrap = entryWrap;

        EJNode ejNode = EJNode.get();
        _ioMgr = ejNode.getIoMgr();
        _logger = ejNode.getLogger();
        _workerMgr = ejNode.getWorkerMgr();
        _tcpServerIdCnt = 0;
        _tcpClientIdCnt = 0;

    }

    @Override
    public HttpHelper http() {
        if(_httpHelperWrap == null){
            _httpHelperWrap = new HttpHelperWrap(this, _nodeCtx);
        }
        return _httpHelperWrap;
    }

    @Override
    public WebsocketHelper websocket() {
        if(_wsHelperWrap == null){
            _wsHelperWrap = new WsHelperWrap(this);
        }
        return _wsHelperWrap;
    }

    @Override
    public TcpServer createServer(int port) {
        return createServer(null, port);
    }

    @Override
    public TcpServer createServer(String host, int port) {
        TcpServerWrap wrap = new TcpServerWrap(++_tcpServerIdCnt, host, port, this);
        return wrap;
    }

    @Override
    public TcpClient createClient(String host, int port) {
        TcpClientWrap wrap = new TcpClientWrap(++_tcpClientIdCnt, host, port, this);
        return wrap;
    }

    public void onTcpServerStart(TcpServerWrap wrap){
        _mapTcpServer.put(wrap.id, wrap);
    }
    public void onTcpServerClose(TcpServerWrap wrap){
        _mapTcpServer.remove(wrap.id);
    }

    public void onTcpClientStart(TcpClientWrap wrap){
        _mapTcpClient.put(wrap.id, wrap);
    }

    public void onTcpClientClose(TcpClientWrap wrap){
        _mapTcpClient.remove(wrap.id);
    }

    protected void onIoMsg(MsgIO msg){
        int type = msg.type;
        if(type == MsgIO.TYPE_MSG){
            TcpChannelWrap chWrap = _lsOnReadChannel.get(msg.session);
            if(chWrap != null){
//                int codecType = chWrap.codecType;
//                if(codecType == NetCodec.TYPE_WEBSOCKET_SERVER){
//                    chWrap.onReadData(msg.userData);
//                }else if(codecType == NetCodec.TYPE_HTTP_SERVER){
//                    HttpReqRecvWrap reqWrap = (HttpReqRecvWrap) msg.userData;
//                    reqWrap.setNodeCtx(_nodeCtx);
//                    chWrap.onReadData(reqWrap);
//                }else if(codecType == NetCodec.TYPE_HTTP_CLIENT){
//                    HttpRspRecvWrap rspWrap = (HttpRspRecvWrap) msg.userData;
//                    rspWrap.setNodeCtx(_nodeCtx);
//                    chWrap.onReadData(rspWrap);
//                }else{
//                    chWrap.onReadData(msg.userData);
//                }

                chWrap.onReadData(msg.userData);
            }
            if(msg.needRelease){ // release msg
                _releaseIoData(msg);
            }
        }
        else if(type == MsgIO.TYPE_SERVER_ACCEPT){
            IoHandler ioHandler = (IoHandler) msg.userData;
            Channel channel = ioHandler.getChannel();
            boolean closeChannel = true;
            TcpServerWrap wrap = _mapTcpServer.get(msg.session);
            if(wrap != null){
                if(wrap.hasSetCbAccept()){
                    TcpChannelWrap chWrap = new TcpChannelWrap(s_channelIdCnt.incrementAndGet(), channel, ioHandler, wrap.codecType());
                    chWrap.setNetWrap(this);
                    closeChannel = false;
                    _mapTempChannel.put(chWrap.id, chWrap);
                    try {
                        wrap.onChannelAccept(chWrap);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
            if(closeChannel){
                channel.close();
            }
        }
        else if(type == MsgIO.TYPE_SEND_CHANNEL){
            ChannelSenderWrap senderWrap = (ChannelSenderWrap) msg.userData;
            TcpChannelWrap chWrap = senderWrap.channelWrap;
            chWrap.setNetWrap(this);
            _mapTempChannel.put(chWrap.id, chWrap);
            // notify
            if(senderWrap.cbConnIn != null){
                try {
                    senderWrap.cbConnIn.onCallback(chWrap);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
            if(senderWrap.cbRead != null){
                try {
                    chWrap.onReadFull(senderWrap.cbRead);
                }catch (StatusIllegalException e){
                    e.printStackTrace();
                }
            }
            if(senderWrap.cbClose != null){
                try {
                    chWrap.onCloseFull(senderWrap.cbClose);
                }catch (StatusIllegalException e){
                    e.printStackTrace();
                }
            }
            if(senderWrap.cbError != null){
                try {
                    chWrap.onErrorFull(senderWrap.cbError);
                } catch (StatusIllegalException e) {
                    e.printStackTrace();
                }
            }
            if(senderWrap.cbHandshakeDone != null){
                try {
                    chWrap.onHandshakeDone();
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }
        else if(type == MsgIO.TYPE_CHANNEL_EVENT){
            int event = msg.event;
            if(event == MsgIO.EVENT_CHANNEL_GONE){   // conn gone
                try {
                    TcpChannelWrap chWrap = _lsOnReadChannel.remove(msg.session);
                    if(chWrap != null){
                        chWrap.onConnGone();
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
            else if(event == MsgIO.EVENT_CHANNEL_HANDSHAKE_DONE){
                TcpChannelWrap chWrap = _lsOnReadChannel.get(msg.session);
                if(chWrap != null){
                    try {
                        chWrap.onHandshakeDone();
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
            else if(event == MsgIO.EVENT_CHANNEL_ERROR){  // channel error
                TcpChannelWrap chWrap = _lsOnReadChannel.get(msg.session);
                if(chWrap != null){
                    try {
                        chWrap.onChannelError((String) msg.userData);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
        }
        else if(type == MsgIO.TYPE_LISTEN_SUCC){
            TcpServerWrap wrap = _mapTcpServer.get(msg.session);
            if(wrap != null){  // ensure wrap exist
                try {
                    wrap.onListenResult(null, (Channel) msg.userData);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }else if(type == MsgIO.TYPE_LISTEN_FAILED){
            TcpServerWrap wrap = _mapTcpServer.remove(msg.session);
            if(wrap != null){
                try {
                    wrap.onListenResult((String) msg.userData, null);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }else if(type == MsgIO.TYPE_CONNECT_SUCC){
            IoHandler ioHandler = (IoHandler) msg.userData;
            Channel channel = ioHandler.getChannel();
            boolean closeChannel = true;
            TcpClientWrap wrap = _mapTcpClient.remove(msg.session);
            if(wrap != null){
                TcpChannelWrap chWrap = new TcpChannelWrap(s_channelIdCnt.incrementAndGet(), channel, ioHandler, wrap.codecType());
                chWrap.setNetWrap(this);
                closeChannel = false;
                _mapTempChannel.put(chWrap.id, chWrap);
                try {
                    wrap.onConnResult(null, chWrap);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
            if(closeChannel){
                channel.close();
            }
        }
        else if(type == MsgIO.TYPE_CONNECT_FAILED){
            TcpClientWrap wrap = _mapTcpClient.remove(msg.session);
            if(wrap != null){
                try {
                    wrap.onConnResult((String)msg.userData, null);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onExit(){
        _ioMgr = null;
        // close all server channel
        TcpServerWrap[] arr = _mapTcpServer.values().toArray(new TcpServerWrap[0]);
        for(int i=0; i<arr.length; ++i){
            arr[i].close();
        }
        _mapTcpServer.clear();
        // close all tcpChannel
        _lsOnReadChannel.iterator(TcpChannelWrap::release);
        _lsOnReadChannel.clear();
        //
        TcpChannelWrap[] arrChannel = _mapTempChannel.values().toArray(new TcpChannelWrap[0]);
        for(int i=0; i<arrChannel.length; ++i){
            arrChannel[i].release();
        }
        _mapTempChannel.clear();
    }

    public boolean sendChannel(ChannelSenderWrap senderWrap) throws StatusIllegalException {
        boolean ret = false;
        do {
            long dstWorkerId = senderWrap.dstWorkerId;
            LoopWorker dstWorker = _workerMgr.getWorkerByPid(dstWorkerId);
            if(dstWorker == null){  // dstWorker has gone
                break;
            }
            TcpChannelWrap channelWrap = senderWrap.channelWrap;
            if(!_mapTempChannel.containsKey(channelWrap.id)){  // chWrap not belong to curWorker
                throw new StatusIllegalException("current tcpChannel is not belong to currentWorker or has set onRead() in currentWorker");
            }
            MsgIO msg = dstWorker.newMsgIo();
            msg.type = MsgIO.TYPE_SEND_CHANNEL;
            msg.userData = senderWrap;
            channelWrap.setNetWrap(null);
            if(!dstWorker.addIOMsg(msg, true)){   // send to dstWorker failed, dstWorker has gone
                channelWrap.setNetWrap(this);
                break;
            }
            _mapTempChannel.remove(channelWrap.id);

            ret = true;
        }while (false);
        return ret;
    }

    public long onChannelReadStart(TcpChannelWrap chWrap){
        _mapTempChannel.remove(chWrap.id);
        return _lsOnReadChannel.add(chWrap);
    }

    // called from ioThread, notify worker
    public void onChannelActive(Channel channel, long starterId, boolean fromServer, IoHandler ioHandler){
        MsgIO msg = _worker.newMsgIo();
        msg.session = starterId;
        if(fromServer){
            msg.type = MsgIO.TYPE_SERVER_ACCEPT;
        }else{
            msg.type = MsgIO.TYPE_CONNECT_SUCC;
        }
        msg.userData = ioHandler;
        if(!_worker.addIOMsg(msg, true)){   // notify worker failed(worker has gone)
            channel.close();
        }
    }

    // called from ioThread, notify worker
    public boolean onChannelRead(long channelId, Object data, boolean needRelease){
        MsgIO msg = _worker.newMsgIo();
        msg.session = channelId;
        msg.type = MsgIO.TYPE_MSG;
        msg.userData = data;
        return _worker.addIOMsg(msg, needRelease);
    }
    // called from ioThread, notify worker
    public void onChannelInactive(long channelId){
        MsgIO msg = _worker.newMsgIo();
        msg.type = MsgIO.TYPE_CHANNEL_EVENT;
        msg.event = MsgIO.EVENT_CHANNEL_GONE;
        msg.session = channelId;
        _worker.addIOMsg(msg, false);
    }
    // called from ioThread, notify worker
    public void onChannelError(long channelId, String error){
        MsgIO msg = _worker.newMsgIo();
        msg.type = MsgIO.TYPE_CHANNEL_EVENT;
        msg.event = MsgIO.EVENT_CHANNEL_ERROR;
        msg.session = channelId;
        msg.userData = error;
        _worker.addIOMsg(msg, false);
    }
    // called from ioThread, notify worker
    public void onChannelHandshakeDone(long channelId){
        MsgIO msg = _worker.newMsgIo();
        msg.type = MsgIO.TYPE_CHANNEL_EVENT;
        msg.event = MsgIO.EVENT_CHANNEL_HANDSHAKE_DONE;
        msg.session = channelId;
        _worker.addIOMsg(msg, false);
    }

    protected void onReleaseIoMsg(MsgIO msg){
        int type = msg.type;
        if(type == MsgIO.TYPE_MSG){
            if(msg.needRelease){
                _releaseIoData(msg);
            }
        }else if(type == MsgIO.TYPE_SERVER_ACCEPT){
            IoHandler transHandler = (IoHandler) msg.userData;
            Channel channel = transHandler.getChannel();
            channel.close();
        }else if(type == MsgIO.TYPE_LISTEN_SUCC){
            Channel channel = (Channel) msg.userData;
            channel.close();
        }else if(type == MsgIO.TYPE_SEND_CHANNEL){
            ChannelSenderWrap senderWrap = (ChannelSenderWrap)msg.userData;
            senderWrap.channelWrap.release();
        }
    }
    private void _releaseIoData(MsgIO msg){
        Object data = msg.userData;
        if(data instanceof IoDataInWrap){
            ReferenceCountUtil.release(((IoDataInWrap)data).getData());
        }else {
            ReferenceCountUtil.release(data);
        }
        msg.userData = null;
    }

    public void checkListenResult(String error, Channel channel, long svrChannelId){
        // notify worker
        MsgIO msg = _worker.newMsgIo();
        msg.session = svrChannelId;
        boolean isSucc = false;
        if(error == null){  // succ
            msg.type = MsgIO.TYPE_LISTEN_SUCC;
            msg.userData = channel;
            isSucc = true;
        }else{  // failed
            msg.type = MsgIO.TYPE_LISTEN_FAILED;
            msg.userData = error;
        }
        if(!_worker.addIOMsg(msg, isSucc) && isSucc){  // notify worker failed(worker has gone), close channel
            channel.close();
        }
    }

    public void onConnectFailed(String error, long clientId){
        // notify worker
        MsgIO msg = _worker.newMsgIo();
        msg.session = clientId;
        msg.type = MsgIO.TYPE_CONNECT_FAILED;
        msg.userData = error;
        _worker.addIOMsg(msg, false);
    }

    protected void setNodeCtx(NodeContext ctx){
        _nodeCtx = ctx;
    }

    protected void setTimerWrap(EJTimerWrap timerWrap){
        _timerWrap = timerWrap;
    }
    public Timer getTimer(){
        return _timerWrap;
    }
    public EJIoMgr getIoMgr(){
        return _ioMgr;
    }
    public EJLoggerCore getLogger(){
        return _logger;
    }

    public NodeContext nodeCtx(){
        return _nodeCtx;
    }
}
