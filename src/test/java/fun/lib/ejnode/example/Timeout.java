package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

public class Timeout {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)
                .start();
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Timer timer = ctx.timer;

            log.info("onStart");
            timer.timeout(2000, ()->{
                log.info("onTimeout");
            });
            timer.timeout(3000, "myData", userData -> {
                log.info("onTimeout, userData="+userData);
            });
        }
    }
}
