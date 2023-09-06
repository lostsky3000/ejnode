package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.callback.*;
import fun.lib.ejnode.core.net.ServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public final class WebsocketServer extends CommonTcpServer{
    private CbWsConnIn _cbConnIn;
    private boolean _listenCalled;
//    private ChannelWorkerChooser _workerChooser;

    public WebsocketServer(Net net){
        super(net);
        _listenCalled = false;
    }
    public WebsocketServer onConnection(CbWsConnIn cb){
        _cbConnIn = cb;
        return this;
    }

//    public WebsocketServer workerChooser(ChannelWorkerChooser workerChooser){
//        _workerChooser = workerChooser;
//        return this;
//    }

    public void listen(int port, String wsUri){
        listen(port, wsUri, null);
    }

    public void listen(int port, String wsUri, CbCommonResult cbResult){
        if(_listenCalled || closeCalled){
            return ;
        }
        _listenCalled = true;
        cbListenRet = cbResult;
        net.createServer(port)
                .codec(ServerCodec.websocket().uri(wsUri))
                .listen(this::procListenResult)
                .onAccept(channel -> {
                    if(closeCalled){
                        channel.close();
                        return;
                    }
                    if(_cbConnIn != null){
//                        boolean useCurWorker = true;
//                        if(_workerChooser != null){
//                            long dstWorker = _workerChooser.next(channel);
//                            if(dstWorker > 0 && dstWorker != NodeContext.currentContext().process.pid()){
//                                try {
//                                    final CbWsConnIn cbConnIn = _cbConnIn;
//                                    useCurWorker = !channel.sender()
//                                            .onConnIn(channel1 -> {
//                                                WsSocketWrap sock = new WsSocketWrap(channel1, cbConnIn);
//                                                sock.startInnerRead();
//                                            })
//                                            .send(dstWorker);
//                                } catch (StatusIllegalException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                        if(useCurWorker){
                            WsSocketWrap sock = new WsSocketWrap(channel, _cbConnIn);
                            sock.startInnerRead();
//                        }
                    }else{
                        channel.close();
                    }
                });
    }

    static class WsSocketWrap implements WsSocket{
        protected final TcpChannel channel;
        private final CbWsConnIn _cbSockIn;
        private boolean _closeCalled;
        private CbWsMessage _cbMsg;
        private CbCommon _cbClose;

        protected WsSocketWrap(TcpChannel channel, CbWsConnIn cbSockIn){
            this.channel = channel;
            _cbSockIn = cbSockIn;
            _closeCalled = false;
        }

        private void startInnerRead(){
            try {
                channel.onHandshakeDone(()->{
                    if(_closeCalled){
                        return;
                    }
                    try {
                        _cbSockIn.onConnIn(this);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }).onRead(data -> {
                    if(_closeCalled){
                        return;
                    }
                    if(_cbMsg != null){
                        try {
                            WebsocketFrame inData = (WebsocketFrame) data;
                            _cbMsg.onMessage(inData);
                        }catch (Throwable e){
                            e.printStackTrace();
                        }
                    }
                });
            } catch (StatusIllegalException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() {
            if(_closeCalled){
                return;
            }
            _closeCalled = true;
            channel.close();
        }

        @Override
        public void onMessage(CbWsMessage cb) throws StatusIllegalException {
            _cbMsg = cb;
        }

        @Override
        public void onClose(CbCommon cb) throws StatusIllegalException {
            if(cb != null){
                if(_cbClose == null){
                    channel.onClose(()->{
                        if(_cbClose != null){
                            _cbClose.onCallback();
                        }
                    });
                }
                _cbClose = cb;
            }
        }

        @Override
        public void send(String str) {
            TextWebSocketFrame rsp = new TextWebSocketFrame(str);
            channel.write(rsp);
        }

        @Override
        public void sendThenClose(String str) {
            TextWebSocketFrame rsp = new TextWebSocketFrame(str);
            channel.writeThenClose(rsp);
        }

    }
}
