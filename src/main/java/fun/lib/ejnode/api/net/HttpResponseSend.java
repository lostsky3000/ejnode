package fun.lib.ejnode.api.net;

public abstract class HttpResponseSend {

    public abstract HttpResponseSend setStatus(int statusCode);

    public abstract HttpResponseSend addHeader(String key, String val);

    public abstract HttpResponseSend echo(String content);

    public abstract void end(String content);

    public abstract void end();

}
