package fun.lib.ejnode.api.net;

public interface CodecWebsocketServer extends Codec4Server {

    CodecWebsocketServer reqMaxBytes(int maxBytes);
    CodecWebsocketServer uri(String uri);
}
