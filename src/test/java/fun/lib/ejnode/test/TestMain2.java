package fun.lib.ejnode.test;

import fun.lib.ejnode.api.*;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.net.ClientCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class TestMain2 {

    public static void main(String[] args){

        EJNode node = EJNode.get();
        node.entry(HttpClientTest1.class)
//                .logLevel(Logger.LEVEL_DEBUG)
                .start();

    }

    static class HttpClientTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            log.info("HttpClientTest1 onStart()");

            String url = "https://www.baidu.com";
            url = "http://127.0.0.1:10086/test1/test2?a=1&b=2";
            net.http().createClient(url)
                    .connTimeout(5000)
                    .onRequest((error, req) -> {
                        log.info("onRequest: error="+error);
                        if(error != null){
                            return;
                        }
                        req.addParam("c", "3")
                                .setMethod("post");
//                        req.setMethod("post")
//                            .writeContent("hehe test");
                    })
                    .query((error, rsp) -> {
                        log.info("onResponse: error="+error);
                        if(error != null){
                            return;
                        }
                        log.info("rsp: status="+rsp.status()+", content="+rsp.contentAsString());
                    })
                    .onClose(()->{
                        log.info("conn gone");
                    });
        }
    }

    static class WsClientTest1 extends NodeEntry {
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;

            log.info("WsClientTest1 onStart()");
            String host = "127.0.0.1";
            int port = 10086;
            net.createClient(host,port)
                    .codec(ClientCodec.websocket().uri("ws://127.0.0.1:10086/ws"))
                    .connect((error, channel) -> {
                        if(error != null){   // conn failed
                            log.error("conn failed: "+error);
                            return;
                        }
                        try {
                            channel.onRead(data -> {
                                log.info("onRead: "+data);
                            });
                            channel.onClose(()->{
                                log.info("conn gone");
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                        log.info("will send frame");
                        WebSocketFrame frame = net.websocket().createTextFrame("heheda");
                        channel.write(frame);
                    });

        }
    }

}
