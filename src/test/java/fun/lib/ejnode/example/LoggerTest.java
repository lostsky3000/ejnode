package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

/**
 * 日志 示例
 * @author lostsky
 */
public class LoggerTest {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)  // 设置启动入口类
                .logLevel(Logger.LEVEL_INFO)  // 设置 logger 级别
                .start();  // 开始事件循环
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;

            log.trace("log by trace");
            log.debug("log by debug");
            log.info("log by info");
            log.warn("log by warn");
            log.error("log by error");
            log.fatal("log by fatal");
        }
    }
}
