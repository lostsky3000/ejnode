package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.callback.CbHttpServerReq;

public abstract class HttpHelper {

    public abstract HttpServer createServer(CbHttpServerReq cb);

    public abstract HttpResponseSend createReponse(int statusCode, HttpRequestRecv req, TcpChannel channel);

    public abstract HttpResponseSend createReponse(int statusCode, TcpChannel channel);

    public abstract HttpRequestSend createRequest(String path, String queryString);

    public abstract HttpRequestSend createRequest(String path);

    public abstract HttpClient createClient(String url);

}

