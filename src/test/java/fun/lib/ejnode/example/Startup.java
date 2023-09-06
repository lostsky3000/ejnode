package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;

public class Startup {

    public static void main(String[] args){
        EJNode node = EJNode.get();
        node.entry(Entry.class)
                .start();
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            log.info("onStart");
        }
    }
}
