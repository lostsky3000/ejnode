package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * 定时任务 示例
 * @author lostsky
 */

public class Schedule {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)  // 设置启动入口类
                .start();  // 开始事件循环
    }

    static class Entry extends NodeEntry{
        private int scheduleCount = 0;
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Timer timer = ctx.timer;

            //开启定时任务，每1000ms执行一次
            timer.schedule(1000, task -> {
                log.info("onSchedule, scheduleCount="+scheduleCount);
                if(++scheduleCount > 10){
                    task.cancel();   //结束当前定时任务
                }
            });
        }
    }
}
