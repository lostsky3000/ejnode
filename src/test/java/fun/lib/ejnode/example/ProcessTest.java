package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Process;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

public class ProcessTest {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)
                .start();
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Process process = ctx.process;

            log.info("Entry onStart, currentProcess: id="+process.pid()+", name="+process.name);

            long pidAnother = process.fork(Another.class);
            ctx.timer.timeout(3000, ()->{
                log.info("will kill another, pid="+pidAnother);
                process.kill(pidAnother);

                //exit self
                log.info("will exit self");
                process.exit();
            });
        }
    }

    static class Another extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Process process = ctx.process;

            log.info("Another onStart, currentProcess: id="+process.pid()+", name="+process.name);
        }

        @Override
        public void onExit(NodeContext ctx) {
            ctx.logger.info("Another onExit, pid="+ctx.process.pid()+", name="+ctx.process.name);
        }
    }
}
