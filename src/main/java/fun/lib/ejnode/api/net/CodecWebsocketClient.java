package fun.lib.ejnode.api.net;

public interface CodecWebsocketClient extends Codec4Client{
    CodecWebsocketClient reqMaxBytes(int maxBytes);
    CodecWebsocketClient uri(String uri);
}
