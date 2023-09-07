package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * 定时器 示例
 * @author lostsky
 */

public class Timeout {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)  // 设置启动入口类
                .start();  // 开始事件循环
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Timer timer = ctx.timer;

            log.info("onStart");
            // 2000ms后执行
            timer.timeout(2000, ()->{
                log.info("onTimeout");
            });
            // 3000ms后执行，带回调参数
            timer.timeout(3000, "myData", userData -> {
                log.info("onTimeout, userData="+userData);
            });
        }
    }
}
