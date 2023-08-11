package fun.lib.ejnode.core;


import fun.lib.ejnode.api.*;
import fun.lib.ejnode.api.Process;

public abstract class NodeContext {

    public final Timer timer;
    public final Logger logger;
    public final Process process;
    public final Net net;
    public final Environment environment;
    public final NodeEntry entry;
    public final Db db;

    protected NodeContext(Timer timer, Logger logger, Process process, Net net, Db db, Environment environment, NodeEntry entry) {
        this.timer = timer;
        this.logger = logger;
        this.process = process;
        this.net = net;
        this.db = db;
        this.environment = environment;
        this.entry = entry;
    }

    /**
     *
     */
    public abstract void shutdown();

    public static NodeContext currentContext(){
        return s_thNodeCtx.get();
    }

    protected static final ThreadLocal<NodeContext> s_thNodeCtx = new ThreadLocal<>();

}
