package fun.lib.ejnode.util.container;

public final class DFPooledLinkedList<T> {

    private final EleWrap<T> _rootUse;
    private final EleWrap<T> _rootPool;
    private int _size;

    public DFPooledLinkedList(int capacity){
        _rootUse = new EleWrap<T>();
        _rootUse.prev = _rootUse;
        _rootUse.next = _rootUse;
        _rootPool = new EleWrap<T>();
        _rootPool.prev = _rootPool;
        _rootPool.next = _rootPool;
        _size = 0;
        for(int i=0; i<capacity; ++i){
            _offerWrap(_rootPool, new EleWrap<T>());
        }
    }
    public DFPooledLinkedList(){
        this(0);
    }

    public void offer(T ele){
        // alloc new wrap
        EleWrap<T> wrap = _pollWrap(_rootPool);
        if(wrap == null){  // no pooled wrap, create
            wrap = new EleWrap<T>();
        }
        wrap.ele = ele;
        _offerWrap(_rootUse, wrap);
        ++_size;
    }

    public T poll(){
        EleWrap<T> wrap = _pollWrap(_rootUse);
        if(wrap != null){
            T ele = wrap.ele;
            wrap.ele = null;
            _offerWrap(_rootPool, wrap);
            --_size;
            return ele;
        }
        return null;
    }

    public int size(){
        return _size;
    }

    private EleWrap<T> _pollWrap(EleWrap<T> root){
        EleWrap<T> head = root.next;
        if(head != root){
            head.next.prev = head.prev;
            head.prev.next = head.next;
            head.prev = null;
            head.next = null;
            return head;
        }
        return null;
    }
    private void _offerWrap(EleWrap<T> root, EleWrap<T> wrap){
        EleWrap<T> tail = root.prev;
        tail.next = wrap;
        root.prev = wrap;
        //
        wrap.prev = tail;
        wrap.next = root;
    }

    static class EleWrap<T>{
        protected EleWrap<T> prev;
        protected EleWrap<T> next;
        protected T ele;
    }
}







