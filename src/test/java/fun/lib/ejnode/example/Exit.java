package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Process;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * 关闭ejnode 示例
 * @author lostsky
 */

public class Exit {

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
            Timer timer = ctx.timer;

            log.info("onStart, will exit after 3 seconds");
            timer.timeout(3000, ()->{
                log.info("exit now");
                process.exit();   // 退出当前process(线程)，所有业务线程退出后，ejnode将释放所有资源并退出
            });
        }
    }
}
