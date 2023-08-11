package fun.lib.ejnode.api.net;

import fun.lib.ejnode.core.NodeContext;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.Map;

public abstract class HttpRequestRecv {

    public abstract String version();

    public abstract String method();

    public abstract String uri();

    public abstract String queryPath();

    public abstract Map<String,String> params();

    public abstract String contentAsString();

    public abstract byte[] contentAsBytes();

    public abstract HttpHeaders headers();

}
