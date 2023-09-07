package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * NextTick 示例
 * @author lostsky
 */
public class NextTick {

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

            // 将回调添加到当前业务线程的`nextTick队列`队尾，业务线程在每一次事件循环的开始，会首先将该队列的回调全部取出执行
            // 参照nodejs的nextTick
            timer.nextTick(()->{
                log.info("onNextTick");
            });
        }
    }
}
