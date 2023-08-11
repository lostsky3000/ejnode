package fun.lib.ejnode.api;

import fun.lib.ejnode.core.NodeContext;

public abstract class NodeEntry {

    public abstract void onStart(NodeContext ctx, Object param);

    public void onExit(NodeContext ctx){}

    public void onMessage(long pidSrc, Object msg){}

}
