package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.callback.CbCommonResult;
import fun.lib.ejnode.api.callback.CbHttpServerReq;
import fun.lib.ejnode.core.net.ServerCodec;

public final class HttpServer extends CommonTcpServer{

    private CbHttpServerReq _cbReq;
    private boolean _listenCalled;
    private ChannelWorkerChooser _workerChooser;

    public HttpServer(CbHttpServerReq cbReq, Net net){
        super(net);
        _cbReq = cbReq;
        _listenCalled = false;
    }

    public HttpServer workerChooser(ChannelWorkerChooser workerChooser){
        if(_workerChooser == null){
            _workerChooser = workerChooser;
        }
        return this;
    }

    public void listen(int port){
        listen(port, null);
    }

    public void listen(int port, CbCommonResult cbResult){
        if(_listenCalled || closeCalled){
            return ;
        }
        _listenCalled = true;
        cbListenRet = cbResult;
        net.createServer(port)
                .codec(ServerCodec.http())
                .listen(this::procListenResult)
                .onAccept(channel -> {
                    if(closeCalled){
                        channel.close();
                        return;
                    }
                    if(_cbReq != null){
                        boolean useCurWorker = true;
                        if(_workerChooser != null){
                            NodeContext ctx = NodeContext.currentContext();
                            long dstWorker = _workerChooser.next(channel);
                            if(dstWorker > 0 && dstWorker != ctx.process.pid()){   // send to another worker
                                try {
                                    final CbHttpServerReq cbReq = _cbReq;
                                    useCurWorker = !channel.sender()
                                            .onRead(((data, channel1) -> {
                                                _procReqCb(data, channel1, NodeContext.currentContext().net, cbReq);
                                            })).send(dstWorker);
                                } catch (StatusIllegalException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if(useCurWorker){
                            try {
                                channel.onRead(data -> {
                                    _procReqCb(data, channel, net, _cbReq);
                                });
                            } catch (StatusIllegalException e) {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        channel.close();
                    }
                });
    }

    private void _procReqCb(Object data, TcpChannel channel, Net net, CbHttpServerReq cb){
        HttpRequestRecv req = (HttpRequestRecv) data;
        HttpResponseSend rsp = net.http().createReponse(200, req, channel);
        try {
            cb.onRequest(req, rsp);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

}
