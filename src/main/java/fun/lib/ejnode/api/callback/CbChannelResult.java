package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.TcpChannel;

public interface CbChannelResult {

    void onCallback(String error, TcpChannel channel);

}
