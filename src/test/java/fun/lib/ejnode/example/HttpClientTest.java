package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

public class HttpClientTest {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)
                .start();
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;

            String url = "https://www.baidu.com/";
            net.http().createClient(url)
                    .connTimeout(10000)
                    .responseTimeout(10000)
                    .onRequest((error, req) -> {
                        if(error != null){  // conn failed
                            log.error("error onRequest: "+error);
                            return;
                        }
                        req.setMethod("GET")
                                .addParam("key1","val1")
                                .addHeader("header1","value1");
                    })
                    .query((error, rsp) -> {
                        if(error != null){
                            log.error("rsp error: "+error);
                            return;
                        }
                        log.info("recv rsp, status="+rsp.status()+", content="+rsp.contentAsString());
                    });
        }
    }
}

