package fun.lib.ejnode.util.container;

import java.util.ArrayList;

public final class DFIdxVerList<T> {

    private final ArrayList<ItemSlot<T>> _ls;
    private final DFPooledLinkedList<Integer> _lsFreeIdx;
    private int _size;

    public DFIdxVerList(){
        _ls = new ArrayList<>();
        _lsFreeIdx = new DFPooledLinkedList<>();
        _size = 0;
    }

    public long add(T val){
        ItemSlot<T> slot;
        Integer idxFree = _lsFreeIdx.poll();
        if(idxFree == null){   // no free slot, add
            int oldSize = _ls.size();
            slot = new ItemSlot<>(oldSize);
            _ls.add(slot);
        }else{
            slot = _ls.get(idxFree);
        }
        slot.use(val);
        ++_size;
        return slot.id();
    }
    public T remove(long id){
        int idx = s_idxFromId(id);
        int ver = s_verFromId(id);
        if(idx >= 0 && idx < _ls.size()){  // idx valid
            ItemSlot<T> slot = _ls.get(idx);
            if(slot.version == ver){  // match
                T v = slot.val;
                slot.clear();
                _lsFreeIdx.offer(idx);
                --_size;
                return v;
            }
        }
        return null;
    }

    public T get(long id){
        int idx = s_idxFromId(id);
        int ver = s_verFromId(id);
        ItemSlot<T> slot = _ls.get(idx);
        if(slot.version == ver){
            return slot.val;
        }
        return null;
    }

    public int size(){
        return _size;
    }
    public void iterator(DFIdxVerListIterator<T> cb){
        int size = _ls.size();
        ItemSlot<T> slot;
        try {
            for(int i=0; i<size; ++i){
                slot = _ls.get(i);
                if(slot.inUse){
                    cb.onIterator(slot.val);
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void clear(){
        int size = _ls.size();
        ItemSlot<T> slot;
        for(int i=0; i<size; ++i){
            slot = _ls.get(i);
            if(slot.inUse){
                slot.clear();
            }
            _lsFreeIdx.offer(slot.idx);
        }
        _size = 0;
    }

    private static class ItemSlot<T>{
        protected final int idx;
        protected int version;
        private long _id;
        protected T val;
        protected boolean inUse;

        protected ItemSlot(int idx){
            this.idx = idx;
            version = 1;
        }

        protected void use(T val){
            this.val = val;
            inUse = true;
            // update id
            _id = s_makeId(idx, version);
        }
        protected void clear(){
            inUse = false;
            if(++version == Integer.MAX_VALUE){
                version = 1;
            }
            val = null;
        }

        protected long id() {
            return _id;
        }

    }

    static long s_makeId(int idx, int ver){
        return ((long) idx <<32) | ver;
    }
    static int s_idxFromId(long id){
        return (int)(id>>32);
    }
    static int s_verFromId(long id){
        return (int)(id&MASK_VER);
    }
    private static final long MASK_VER = 0xffffffff;

    public interface DFIdxVerListIterator<T>{
        void onIterator(T val);
    }
}
