package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Iterator;
import java.util.Map;

/**
 * http服务器 示例
 * @author lostsky
 */

public class HttpServerTest {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)  // 设置启动入口类
                .start();  // 开始事件循环
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            Timer timer = ctx.timer;

            int port = 10086;
            net.http().createServer((req, rsp) -> {
                log.info("======================");
                String method = req.method();   //获取http method
                log.info("recv http request, method="+method+", queryPath="+req.queryPath());
                HttpHeaders headers = req.headers();  //获取request的headers
                Iterator<Map.Entry<String, String>> itHeader = headers.iteratorAsString();
                while (itHeader.hasNext()){
                    Map.Entry<String, String> en = itHeader.next();
                    log.info("reqHeader "+en.getKey()+" = "+en.getValue());
                }
                log.info("======================");
                if(method.equals("GET")){   // GET
                    Map<String,String> params = req.params();   // method为GET时，获取queryString解析出来的参数  e.g: .../test?a=1&b=2
                    Iterator<Map.Entry<String,String>> itParam = params.entrySet().iterator();
                    while (itParam.hasNext()){
                        Map.Entry<String,String> en = itParam.next();
                        log.info("reqParam "+en.getKey()+" = "+en.getValue());
                    }
                }else {  // POST
                    // method为POST时，获取post body
                    log.info("reqContent="+req.contentAsString());
                }
                //response
                timer.timeout(100, ()->{   //模拟服务器数据操作后，异步响应
                    rsp.addHeader("header1", "value1")
                            .echo("echo from server 1<br/>")
                            .echo("echo from server 2<br/>")
                            .end("echo from server 3");
                });
            }).listen(10086, error -> {   //启动监听
                if(error != null){  //监听失败
                    log.error("listen at "+port+" failed: "+error);
                    return;
                }
                log.info("listen at "+port+" succ");
            });
        }
    }
}
