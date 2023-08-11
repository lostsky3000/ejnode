package fun.lib.ejnode.core.net;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.callback.CbHttpServerReq;
import fun.lib.ejnode.api.net.*;
import fun.lib.ejnode.core.EJNetWrap;
import io.netty.handler.codec.http.*;

public final class HttpHelperWrap extends HttpHelper {

    private EJNetWrap _netWrap;
    private NodeContext _nodeCtx;

    public HttpHelperWrap(EJNetWrap netWrap, NodeContext nodeCtx){
        _netWrap = netWrap;
        _nodeCtx = nodeCtx;
    }

    @Override
    public HttpServer createServer(CbHttpServerReq cb) {
        HttpServer svr = new HttpServer(cb, _netWrap);
        return svr;
    }

    @Override
    public HttpResponseSend createReponse(int statusCode, HttpRequestRecv req, TcpChannel channel) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(statusCode);
        FullHttpResponse rsp;
        if(req == null){
            rsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        }else{
            HttpReqRecvWrap reqWrap = (HttpReqRecvWrap) req;
            rsp = new DefaultFullHttpResponse(reqWrap.rawVersion(), status);
        }
        HttpRspSendWrap rspWrap = new HttpRspSendWrap(rsp, channel);
        return rspWrap;
    }

    @Override
    public HttpResponseSend createReponse(int statusCode, TcpChannel channel) {
        return createReponse(statusCode, null, channel);
    }

    @Override
    public HttpRequestSend createRequest(String path, String queryString) {
//        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method), uri);
        HttpReqSendWrap reqWrap = new HttpReqSendWrap(path, queryString);
        return reqWrap;
    }

    @Override
    public HttpRequestSend createRequest(String path) {
        return createRequest(path, null);
    }

    @Override
    public HttpClient createClient(String url) {
        HttpClient client = new HttpClient(url, _netWrap, _netWrap.getTimer());
        return client;
    }

}

