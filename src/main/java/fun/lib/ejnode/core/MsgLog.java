package fun.lib.ejnode.core;


import fun.lib.ejnode.util.container.DFPooledLinkedList;

public final class MsgLog {

    private final DFPooledLinkedList<LogItem> _queueItem;
    private final DFPooledLinkedList<LogItem> _itemPool;

    protected MsgLog(){
        _queueItem = new DFPooledLinkedList<>();
        _itemPool = new DFPooledLinkedList<>();
    }

    public void addItem(String tag, String str, int level){
        LogItem item = _itemPool.poll();
        if(item == null){
            item = new LogItem();
        }
        item.reset(tag, str, level);
        _queueItem.offer(item);
    }

    protected LogItem pollItem(){
        return _queueItem.poll();
    }

    protected void recycleItem(LogItem item){
        item.reset(null, null, 0);
        _itemPool.offer(item);
    }

    protected class LogItem{
        protected int level;
        protected String tag;
        protected String str;
        protected void reset(String tag, String str, int level){
            this.tag = tag;
            this.str = str;
            this.level = level;
        }
    }
}
