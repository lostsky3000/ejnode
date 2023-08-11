package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.callback.CbChannel;
import fun.lib.ejnode.api.callback.CbServerResult;

public interface TcpServerFuture {

    TcpServerFuture onAccept(CbChannel cb);

}
