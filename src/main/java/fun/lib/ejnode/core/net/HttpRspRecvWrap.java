package fun.lib.ejnode.core.net;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.net.HttpResponseRecv;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;

public final class HttpRspRecvWrap extends HttpResponseRecv implements IoDataInWrap {

    private final FullHttpResponse _rsp;
    private String _decErr;
    private int _status;
    private String _contentAsStr;
    private byte[] _contentAsBytes;

    public HttpRspRecvWrap(FullHttpResponse rsp){
        _rsp = rsp;
    }

    @Override
    public String decodeError() {
        if(_decErr == null){
            if(!_rsp.decoderResult().isSuccess()){
                _decErr = _rsp.decoderResult().cause().toString();
            }
        }
        return _decErr;
    }

    @Override
    public int status() {
        if(_status == 0){
            _status = _rsp.status().code();
        }
        return _status;
    }

    @Override
    public String contentAsString() {
        if(_checkDecErr()){
            return null;
        }
        if(_contentAsStr == null){
            ByteBuf buf = _rsp.content();
            _contentAsStr = buf.getCharSequence(buf.readerIndex(), buf.readableBytes(), CharsetUtil.UTF_8).toString();
        }
        return _contentAsStr;
    }

    @Override
    public byte[] contentAsBytes() {
        if(_checkDecErr()){
            return null;
        }
        if(_contentAsBytes == null){
            ByteBuf buf = _rsp.content();
            _contentAsBytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), _contentAsBytes);
        }
        return _contentAsBytes;
    }

    @Override
    public HttpHeaders headers() {
        if(_checkDecErr()){
            return null;
        }
        return _rsp.headers();
    }

    private boolean _checkDecErr(){
        return decodeError() != null;
    }

    @Override
    public Object getData() {
        return _rsp;
    }
}
