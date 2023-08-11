package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.HttpRequestRecv;
import fun.lib.ejnode.api.net.HttpResponseSend;

public interface CbHttpServerReq {

    void onRequest(HttpRequestRecv req, HttpResponseSend rsp);

}
