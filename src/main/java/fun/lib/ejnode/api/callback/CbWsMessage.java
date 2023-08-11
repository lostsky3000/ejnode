package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.net.WebsocketFrame;

public interface CbWsMessage {
    void onMessage(WebsocketFrame msg);
}
