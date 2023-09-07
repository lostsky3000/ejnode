package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * http客户端 示例
 * @author lostsky
 */
public class HttpClientTest {

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

            String url = "https://www.baidu.com/";
            net.http().createClient(url)
                    .connTimeout(10000)    // 设置连接超时(ms)
                    .responseTimeout(10000)  // 设置服务器响应超时(ms)
                    .onRequest((error, req) -> {  // 连接服务器有结果时回调
                        if(error != null){  // 连接服务器发生错误
                            log.error("error onRequest: "+error);
                            return;
                        }
                        //连接服务器成功，设置request参数
                        req.setMethod("GET")
                                .addParam("key1","val1")
                                .addHeader("header1","value1");
                    })
                    .query((error, rsp) -> {  //request发送后回调
                        if(error != null){
                            log.error("rsp error: "+error);
                            return;
                        }
                        //获取到服务器的响应，解析
                        log.info("recv rsp, status="+rsp.status()+", content="+rsp.contentAsString());
                    });
        }
    }
}

