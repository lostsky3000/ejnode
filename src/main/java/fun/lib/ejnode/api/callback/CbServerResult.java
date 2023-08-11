package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.TcpServerChannel;

public interface CbServerResult {
    void onResult(String error, TcpServerChannel channel);
}
