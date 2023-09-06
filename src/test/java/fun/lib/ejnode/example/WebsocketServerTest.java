package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

public class WebsocketServerTest {

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

            int port = 10086;
            String uri = "/ws";   // ws://127.0.0.1:10086/ws
            net.websocket().createServer()
                    .onConnection(socket -> {
                        log.info("connection in");
                        try {
                            socket.onMessage(msg -> {
                                if(msg.isText()){  // TextFrame
                                    log.info("recvMsg: "+msg.text());
                                }
                                socket.send("echo from server");
                                socket.close();
                            });
                            socket.onClose(()->{
                                log.info("connection closed");
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                    })
                    .listen(port, uri, error -> {
                        if(error != null){    // listen failed
                            log.error("listen at "+port+" failed: "+error);
                            return;
                        }
                        log.info("listen at "+port+" succ");
                    });
        }
    }
}
