package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.TcpChannel;

public interface CbChannel {
    void onCallback(TcpChannel channel);
}
