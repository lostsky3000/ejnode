package fun.lib.ejnode.util.container;

import java.util.concurrent.locks.ReentrantLock;

public final class DFObjPool<T> {

    private final ReentrantLock _lock;
    private final DFPooledLinkedList<T> _pool;
//    private final Class<T> _clz;
    private final int _maxSize;

    public DFObjPool(int maxSize){
//        _clz = clz;
        _maxSize = maxSize;
        _lock = new ReentrantLock();
        _pool = new DFPooledLinkedList<>();
    }

    public T newObj() {
        T obj;
        if(_lock.tryLock()){
            try {
                obj = _pool.poll();
                return obj;
            }finally {
                _lock.unlock();
            }
        }
//        if(obj == null){
//            try {
//                obj = _clz.newInstance();
//            } catch (InstantiationException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
        return null;
    }

    public void recycle(T obj){
        if(_lock.tryLock()){
            try {
                if(_pool.size() >= _maxSize){
                    return;
                }
                _pool.offer(obj);
            }finally {
                _lock.unlock();
            }
        }
    }

}
