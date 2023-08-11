package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.HttpRequestSend;

public interface CbHttpClientReq {

    void onRequest(String error, HttpRequestSend req);

}
