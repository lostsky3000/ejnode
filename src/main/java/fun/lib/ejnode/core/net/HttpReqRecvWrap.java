package fun.lib.ejnode.core.net;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.net.HttpRequestRecv;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class HttpReqRecvWrap extends HttpRequestRecv implements IoDataInWrap {

    private final FullHttpRequest _req;
    //
    private String _contentAsStr;
    private byte[] _contentAsBytes;

    private QueryStringDecoder _queryStrDec;

    private HashMap<String,String> _mapParam;

    public HttpReqRecvWrap(FullHttpRequest req){
        _req = req;
    }

    @Override
    public String version() {
        return _req.protocolVersion().text();
    }

    @Override
    public String method() {
        return _req.method().name();
    }

    @Override
    public String uri() {
        return _req.uri();
    }

    @Override
    public String queryPath() {
        _ensureQueryStrDec();
        return _queryStrDec.path();
    }

    @Override
    public Map<String, String> params() {
        if(_req.method() == HttpMethod.POST){  // POST
            if(_mapParam == null){
                _mapParam = new HashMap<>();
                String contentType = headers().get("Content-Type");
                if(contentType != null){
                    contentType = contentType.trim().toLowerCase();
                    if(contentType.contains("x-www-form-urlencoded")){
                        HttpPostRequestDecoder dec = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false),_req);
                        List<InterfaceHttpData> lsParam = dec.getBodyHttpDatas();
                        if(lsParam != null && lsParam.size() > 0){
                            for(InterfaceHttpData param : lsParam){
                                if(param.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute){
                                    MemoryAttribute data = (MemoryAttribute) param;
                                    _mapParam.put(data.getName(), data.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }else{  // GET
            if(_mapParam == null){
                _ensureQueryStrDec();
                Map<String, List<String>> mapRaw= _queryStrDec.parameters();
                if(mapRaw != null && !mapRaw.isEmpty()){
                    _mapParam = new HashMap<>(mapRaw.size());
                    // copy
                    Iterator<Map.Entry<String,List<String>>> itRaw = mapRaw.entrySet().iterator();
                    while (itRaw.hasNext()){
                        Map.Entry<String,List<String>> entry = itRaw.next();
                        _mapParam.put(entry.getKey(), entry.getValue().get(0));
                    }
                }
                if(_mapParam == null){
                    _mapParam = new HashMap<>();
                }
            }
        }
        return _mapParam;
    }
    private void  _ensureQueryStrDec(){
        if(_queryStrDec == null){
            _queryStrDec = new QueryStringDecoder(uri(), CharsetUtil.UTF_8);
        }
    }

    @Override
    public String contentAsString() {
        if(_contentAsStr == null){
            ByteBuf buf = _req.content();
            _contentAsStr = buf.getCharSequence(buf.readerIndex(), buf.readableBytes(), CharsetUtil.UTF_8).toString();
        }
        return _contentAsStr;
    }

    @Override
    public byte[] contentAsBytes() {
        if(_contentAsBytes == null){
            ByteBuf buf = _req.content();
            _contentAsBytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), _contentAsBytes);
        }
        return _contentAsBytes;
    }

    @Override
    public HttpHeaders headers() {
        return _req.headers();
    }

    protected HttpVersion rawVersion(){
        return _req.protocolVersion();
    }

    @Override
    public Object getData() {
        return _req;
    }
}


