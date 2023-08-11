package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.callback.CbCommonResult;

public interface ListenFuture {

    void onResult(CbCommonResult cb);
}
