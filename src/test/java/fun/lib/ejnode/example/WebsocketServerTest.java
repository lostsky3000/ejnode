package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * websocket服务器 示例
 * @author lostsky
 */

public class WebsocketServerTest {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)   // 设置启动入口类
                .start();  // 开始事件循环
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;

            int port = 10086;
            String uri = "/ws";   // e.g: ws://127.0.0.1:10086/ws
            net.websocket().createServer()
                    .onConnection(socket -> {   // 有新的连接进来
                        log.info("connection in");
                        try {
                            socket.onMessage(msg -> {  // 设置接收消息回调
                                if(msg.isText()){  // TextFrame
                                    log.info("recvMsg: "+msg.text());
                                }
                                socket.send("echo from server");
                                socket.close();
                            });
                            socket.onClose(()->{  // 设置连接关闭回调
                                log.info("connection closed");
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                    })
                    .listen(port, uri, error -> {   // 启动监听
                        if(error != null){    // 监听失败
                            log.error("listen at "+port+" failed: "+error);
                            return;
                        }
                        // 监听成功
                        log.info("listen at "+port+" succ");
                    });
        }
    }
}
