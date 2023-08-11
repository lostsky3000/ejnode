package fun.lib.ejnode.api.net;

import java.util.List;

public final class DefaultWorkerChooser implements ChannelWorkerChooser{

    private final long[] _arrWorker;
    private final int _workerNum;
    private int _idxWorker;

    public DefaultWorkerChooser(List<Long> lsWorker){
        _workerNum = lsWorker.size();
        _arrWorker = new long[_workerNum];
        for(int i=0; i<_workerNum; ++i){
            _arrWorker[i] = lsWorker.get(i);
        }
        _idxWorker = 0;
    }

    @Override
    public long next(TcpChannel channel) {
        long id = _arrWorker[_idxWorker%_workerNum];
        if(++_idxWorker == _workerNum){
            _idxWorker = 0;
        }
        return id;
    }
}
