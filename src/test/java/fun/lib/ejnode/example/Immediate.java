package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * Immediate 示例
 * @author lostsky
 */

public class Immediate {

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

            // 将回调添加到当前业务线程的`immediate队列`队尾，业务线程在每一次事件循环会从队列头部取出一个来执行
            // 参照nodejs的immediate
            timer.immediate(()->{
                log.info("onImmediate");
            });
        }
    }
}
