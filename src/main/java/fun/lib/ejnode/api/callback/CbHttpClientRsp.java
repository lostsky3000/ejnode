package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.HttpResponseRecv;

public interface CbHttpClientRsp {
    void onResponse(String error, HttpResponseRecv rsp);
}
