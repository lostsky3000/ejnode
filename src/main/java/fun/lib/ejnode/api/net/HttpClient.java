package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.Process;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.api.callback.CbCommon;
import fun.lib.ejnode.api.callback.CbCommonResult;
import fun.lib.ejnode.api.callback.CbHttpClientReq;
import fun.lib.ejnode.api.callback.CbHttpClientRsp;
import fun.lib.ejnode.core.net.ClientCodec;
import fun.lib.ejnode.core.net.IoDataOutWrap;

import java.net.URL;

public final class HttpClient {

    private final String _url;
    private final Net _net;
    private final Timer _timer;
    private boolean _queryCalled;

    private boolean _connClosed;

    private CbHttpClientReq _cbReq;
    private CbHttpClientRsp _cbRsp;
    private CbCommon _cbClose;

    private long _connTimeout;
    private long _rspTimeout;
    private boolean _cbRspCalled;

    private String _queryPath;
    private String _queryString;

    public HttpClient(String url, Net net, Timer timer){
        _url = url;
        _net = net;
        _timer = timer;
        _queryCalled = false;
        _connClosed = false;
        _cbRspCalled = false;
        _initDefault();
    }

    public HttpClient query(){
        return query(null);
    }

    public HttpClient query(CbHttpClientRsp cbRsp){
        if(_queryCalled){
            return this;
        }
        _queryCalled = true;
        //
        _cbRsp = cbRsp;
        String errorQuery = null;
        do{
            String host = null;
            int port = 0;
            boolean isSsl = false;
            URL urlInfo = null;
            try {
                urlInfo = new URL(_url);
                String protocol = urlInfo.getProtocol();
                if(protocol.equals("https")){
                    isSsl = true;
                }else if(!protocol.equals("http")){
                    errorQuery = "protocol invalid: " + protocol;
                    break;
                }
                host = urlInfo.getHost();
                port = urlInfo.getPort();
                if(port < 0){
                    port = urlInfo.getDefaultPort();
                }
                _queryPath = urlInfo.getPath();
                _queryString = urlInfo.getQuery();
            }catch (Throwable e){
//                e.printStackTrace();
                errorQuery = e.toString();
                break;
            }
            // do connect
            TcpClient client = _net.createClient(host, port);
            if(_connTimeout > 0){
                client.timeout(_connTimeout);
            }
            if(isSsl){
                client.sslBegin().end();
            }
            client.codec(ClientCodec.http())
                    .connect(((error, channel) -> {
                        if(error != null){   // connect error
                            if(_cbReq != null){
                                try {
                                    _cbReq.onRequest(error, null);
                                }catch (Throwable e){
                                    e.printStackTrace();
                                }
                            }
                            return;
                        }
                        // conn succ, send req
                        HttpRequestSend req = _net.http().createRequest(_queryPath, _queryString);
                        try {
                            _cbReq.onRequest(null, req);
                        }catch (Throwable e){
                            e.printStackTrace();
                        }finally {
                            // send req
                            channel.write(req);
                        }
                        try {
                            channel.onError(error1 -> {
                                if(_cbRspCalled){
                                    return;
                                }
                                _cbRspCalled = true;
                                if(_cbRsp != null){
                                    try {
                                        _cbRsp.onResponse(error1, null);
                                    }catch (Throwable e){
                                        e.printStackTrace();
                                    }
                                }
                            }).onRead(data -> {
                                if(_cbRspCalled){
                                    return;
                                }
                                _cbRspCalled = true;
                                try {
                                    if(_cbRsp != null){
                                        HttpResponseRecv rsp = (HttpResponseRecv) data;
                                        _cbRsp.onResponse(null, rsp);
                                    }
                                }catch (Throwable e){
                                    e.printStackTrace();
                                }finally {
                                    channel.close();
                                }
                            }).onClose(()->{
                                if(_connClosed){
                                    return;
                                }
                                _connClosed = true;
                                if(_cbClose != null){
                                    try {
                                        _cbClose.onCallback();
                                    }catch (Throwable e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                        if(_rspTimeout > 0){   // has set responseTimeout
                            _timer.timeout(_rspTimeout, ()->{
                                if(_cbRspCalled){
                                    return;
                                }
                                _cbRspCalled = true;
                                if(!_connClosed){
                                    channel.close();
                                }
                            });
                        }
                    }));

        }while (false);
        if(errorQuery != null){   // has error, notify
            final String err = errorQuery;
            _timer.nextTick(()->{
                if(_cbReq != null){
                    try {
                        _cbReq.onRequest(err, null);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            });
        }
        return this;
    }

    public HttpClient onRequest(CbHttpClientReq cb){
        _cbReq = cb;
        return this;
    }

    public HttpClient connTimeout(long timeoutMs){
        _connTimeout = timeoutMs;
        return this;
    }

    public HttpClient responseTimeout(long timeoutMs){
        _rspTimeout = timeoutMs;
        return this;
    }

    public HttpClient onClose(CbCommon cb){
        _cbClose = cb;
        return this;
    }

    private void _initDefault(){
        _connTimeout = 1000 * 10;
        _rspTimeout = 1000 * 30;
    }

}
