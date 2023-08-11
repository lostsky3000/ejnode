package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.HttpResponseSend;
import fun.lib.ejnode.api.net.TcpChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

public final class HttpRspSendWrap extends HttpResponseSend implements IoDataOutWrap {

    private final FullHttpResponse _rsp;
    private final TcpChannel _channel;
    private boolean _endCalled;

    public HttpRspSendWrap(FullHttpResponse rsp, TcpChannel channel){
        _rsp = rsp;
        _channel = channel;
        _endCalled = false;
    }

    @Override
    public HttpResponseSend setStatus(int statusCode) {
        if(_endCalled){
            return this;
        }
        _rsp.setStatus(HttpResponseStatus.valueOf(statusCode));
        return this;
    }

    @Override
    public HttpResponseSend addHeader(String key, String val) {
        if(_endCalled){
            return this;
        }
        _rsp.headers().add(key, val);
        return this;
    }

    @Override
    public HttpResponseSend echo(String content) {
        if(_endCalled){
            return this;
        }
        _rsp.content().writeCharSequence(content, CharsetUtil.UTF_8);
        return this;
    }

    @Override
    public void end(String content) {
        echo(content);
        end();
    }

    @Override
    public void end() {
        if(_endCalled){
            return;
        }
        _endCalled = true;
        if(!_channel.writeThenClose(this)){  // channel is busy(buffer is full)
            _rsp.release();
        }
    }

    @Override
    public Object getData() {
        return _rsp;
    }
}

