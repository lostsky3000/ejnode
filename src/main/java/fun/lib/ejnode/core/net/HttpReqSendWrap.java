package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.HttpRequestSend;
import fun.lib.ejnode.api.net.TcpChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class HttpReqSendWrap extends HttpRequestSend implements IoDataOutWrap{

    public final String path;
    public final String queryString;
    private HttpRequest _req;
    private ArrayList<String> _lsParam;
    private ArrayList<String> _lsHeader;

    private String _method;

    public HttpReqSendWrap(String path, String queryString){
        this.path = path;
        if(queryString != null && !queryString.equals("")){
            this.queryString = queryString.trim();
        }else{
            this.queryString = null;
        }
    }

    @Override
    public HttpRequestSend setMethod(String method) {
//        _req.setMethod(HttpMethod.valueOf(method));
        if(method != null){
            _method = method.toUpperCase();
        }
        return this;
    }

    @Override
    public HttpRequestSend addHeader(String name, String value) {
//        _req.headers().add(name, value);
        if(_lsHeader == null){
            _lsHeader = new ArrayList<>();
        }
        _lsHeader.add(name);
        _lsHeader.add(value);
        return this;
    }

    @Override
    public HttpRequestSend addParam(String name, String value) {
        if(_lsParam == null){
            _lsParam = new ArrayList<>();
        }
        _lsParam.add(name);
        _lsParam.add(value);
        return this;
    }

    @Override
    public HttpRequestSend writeContent(String content) {
//        _req.content().writeCharSequence(content, CharsetUtil.UTF_8);
        return this;
    }

    public void end(){
        if(_req != null){
            return;
        }
        if(_method == null){
            _method = "GET";
        }
        HttpMethod method = HttpMethod.valueOf(_method);
        if(method == HttpMethod.POST){
            _req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);
            if(_lsParam != null){
                HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
                try {
                    HttpPostRequestEncoder enc = new HttpPostRequestEncoder(factory, _req, false);
                    int size = _lsParam.size();
                    for(int i=0; i<size; i+=2){
                        enc.addBodyAttribute(_lsParam.get(i), _lsParam.get(i+1));
                    }
                    _req = enc.finalizeRequest();
                } catch (HttpPostRequestEncoder.ErrorDataEncoderException e) {
                    e.printStackTrace();
                }
            }
            if(_lsHeader != null){
                int size = _lsHeader.size();
                for(int i=0; i<size; i+=2){
                    _req.headers().add(_lsHeader.get(i), _lsHeader.get(i+1));
                }
            }
        }else{
//            method = HttpMethod.GET;
            Map<String, List<String>> mapParam = null;
            if(queryString != null && !queryString.equals("")){  // has query string
                QueryStringDecoder dec = new QueryStringDecoder(queryString, false);
                mapParam = dec.parameters();
            }
            QueryStringEncoder encUri = new QueryStringEncoder(path);
            if(mapParam != null){
                Iterator<Map.Entry<String,List<String>>> it = mapParam.entrySet().iterator();
                while (it.hasNext()){
                    Map.Entry<String,List<String>> en = it.next();
                    encUri.addParam(en.getKey(), en.getValue().get(0));
                }
            }
            if(_lsParam != null){
                int size = _lsParam.size();
                for(int i=0; i<size; i+=2){
                    encUri.addParam(_lsParam.get(i), _lsParam.get(i+1));
                }
            }
            _req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, encUri.toString());
            if(_lsHeader != null){
                int size = _lsHeader.size();
                for(int i=0; i<size; i+=2){
                    _req.headers().add(_lsHeader.get(i), _lsHeader.get(i+1));
                }
            }
        }
    }

    @Override
    public Object getData() {
        if(_req == null){
            end();
        }
        return _req;
    }
}
