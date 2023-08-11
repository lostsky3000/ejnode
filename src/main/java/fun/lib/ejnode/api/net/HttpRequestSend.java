package fun.lib.ejnode.api.net;

public abstract class HttpRequestSend {

    public abstract HttpRequestSend setMethod(String method);

    public abstract HttpRequestSend addHeader(String name, String value);

    public abstract HttpRequestSend addParam(String name, String value);

    public abstract HttpRequestSend writeContent(String content);

}
