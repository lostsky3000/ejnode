package fun.lib.ejnode.api.net;

import fun.lib.ejnode.core.NodeContext;
import io.netty.handler.codec.http.HttpHeaders;

public abstract class HttpResponseRecv {

    public abstract String decodeError();

    public abstract int status();

    public abstract String contentAsString();

    public abstract byte[] contentAsBytes();

    public abstract HttpHeaders headers();

}


