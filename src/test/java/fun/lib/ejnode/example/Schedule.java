package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

public class Schedule {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)
                .start();
    }

    static class Entry extends NodeEntry{
        private int scheduleCount = 0;
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Timer timer = ctx.timer;

            timer.schedule(1000, task -> {
                log.info("onSchedule, scheduleCount="+scheduleCount);
                if(++scheduleCount > 10){
                    task.cancel();
                }
            });
        }
    }
}
