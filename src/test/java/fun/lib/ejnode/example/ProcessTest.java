package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Process;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * Process 示例
 * @author lostsky
 */
public class ProcessTest {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)  // 设置启动入口类
                .start();  // 开始事件循环
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Process process = ctx.process;

            //每个Process代表一个业务线程, pid为线程id, name为线程名字
            log.info("Entry onStart, currentProcess: id="+process.pid()+", name="+process.name);

            // fork会创建一个新的业务线程
            long pidAnother = process.fork(Another.class);

            ctx.timer.timeout(3000, ()->{
                log.info("will kill another, pid="+pidAnother);
                // kill 会结束指定的业务线程
                process.kill(pidAnother);

                //exit self
                log.info("will exit self");
                // 推出当前业务线程(当所有业务线程都退出后，ejnode将清理所有资源并退出)
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
            // 当前业务线程退出之前，会回调该方法，可用于清理用户自定义资源
            ctx.logger.info("Another onExit, pid="+ctx.process.pid()+", name="+ctx.process.name);
        }
    }
}
