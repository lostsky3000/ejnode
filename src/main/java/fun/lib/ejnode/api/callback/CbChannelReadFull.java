package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.TcpChannel;

public interface CbChannelReadFull {

    void onRead(Object data, TcpChannel channel);
}
