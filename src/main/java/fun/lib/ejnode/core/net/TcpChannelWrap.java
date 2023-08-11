package fun.lib.ejnode.core.net;

import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.callback.*;
import fun.lib.ejnode.api.net.ChannelSender;
import fun.lib.ejnode.api.net.TcpChannel;
import fun.lib.ejnode.core.EJNetWrap;
import fun.lib.ejnode.core.LoopWorker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class TcpChannelWrap extends TcpChannel {

    public final int id;
    private final Channel _channel;
    private final IoHandler _ioHandler;

    public final int codecType;
    private CbChannelReadSimple _cbRead;
    private CbChannelReadFull _cbReadFull;
    private CbCommon _cbGone;
    private CbChannel _cbGoneFull;
    private CbCommonResult _cbError;
    private CbChannelResult _cbErrorFull;
    private CbCommon _cbHandshakeDone;
    private CbChannel _cbHandshakeDoneFull;

    private EJNetWrap _netWrap;
    private boolean _readStarted;
    private final AtomicBoolean _closeCalled = new AtomicBoolean(false);
    private final AtomicInteger _refCnt = new AtomicInteger(1);
    private long _bindWorker4Read;
    private ChannelSenderWrap _senderWrap;
    private long _id;

    public TcpChannelWrap(int id, Channel channel, IoHandler ioHandler, int codecType){
        this.id = id;
        _channel = channel;
        _ioHandler = ioHandler;
        this.codecType = codecType;
        //
        _cbReadFull = null;
        _cbGone = null;
        _cbGoneFull = null;
        _bindWorker4Read = 0;
        _readStarted = false;
        _senderWrap = null;
    }
    public void setNetWrap(EJNetWrap netWrap){
        _netWrap = netWrap;
    }

    private void _startRead(LoopWorker worker){
        if(!_readStarted){
            _readStarted = true;
            _netWrap = worker.getNetWrap();
            _id = _netWrap.onChannelReadStart(this);
            _ioHandler.onReadStart(_netWrap, _id);
        }
    }

    @Override
    public TcpChannel onHandshakeDone(CbCommon cb) throws StatusIllegalException {
        LoopWorker worker = _checkBind4Read("onHandshakeDone()");
        _cbHandshakeDone = cb;
        _startRead(worker);
        return this;
    }

    public TcpChannel onHandshakeDoneFull(CbChannel cb) throws StatusIllegalException {
        LoopWorker worker = _checkBind4Read("onHandshakeDoneFull()");
        _cbHandshakeDoneFull = cb;
        _startRead(worker);
        return this;
    }

    @Override
    public TcpChannel onRead(CbChannelReadSimple cb) throws StatusIllegalException {
        LoopWorker worker = _checkBind4Read("onRead()");
        _cbRead = cb;
        _startRead(worker);
        return this;
    }

    public void onReadFull(CbChannelReadFull cb) throws StatusIllegalException {
        LoopWorker worker = _checkBind4Read("onReadFull()");
        _cbReadFull = cb;
        _startRead(worker);
    }

    @Override
    public TcpChannel onClose(CbCommon cb) throws StatusIllegalException {
        LoopWorker worker = _checkBind4Read("onClose()");
        _cbGone = cb;
        _startRead(worker);
        return this;
    }
    public void onCloseFull(CbChannel cb) throws StatusIllegalException {
        LoopWorker worker = _checkBind4Read("onCloseFull()");
        _cbGoneFull = cb;
        _startRead(worker);
    }

    @Override
    public TcpChannel onError(CbCommonResult cb) throws StatusIllegalException {
        LoopWorker worker = _checkBind4Read("onError()");
        _cbError = cb;
        _startRead(worker);
        return this;
    }
    public void onErrorFull(CbChannelResult cb) throws StatusIllegalException {
        LoopWorker worker = _checkBind4Read("onErrorFull()");
        _cbErrorFull = cb;
        _startRead(worker);
    }

    @Override
    public void close() {
        _doClose();
    }
    private void _doClose(){
        if(_closeCalled.getAndSet(true)){
            return;
        }
        _channel.close();
    }

    @Override
    public boolean write(Object dataObj) {
//        IoDataOutWrap dataWrap = (IoDataOutWrap)dataObj;
//        Object data = dataWrap.getData();
        Object data = null;
        if(dataObj instanceof IoDataOutWrap){
            IoDataOutWrap dataWrap = (IoDataOutWrap)dataObj;
            data = dataWrap.getData();
        }else{
            data = dataObj;
        }

        if(data != null && _channel.isWritable()){
            _channel.writeAndFlush(data);
            return true;
        }
        return false;
    }

    @Override
    public boolean write(Object dataObj, CbCommonResult cb) {
//        IoDataOutWrap dataWrap = (IoDataOutWrap)dataObj;
//        Object data = dataWrap.getData();
        Object data = null;
        if(dataObj instanceof IoDataOutWrap){
            IoDataOutWrap dataWrap = (IoDataOutWrap)dataObj;
            data = dataWrap.getData();
        }else{
            data = dataObj;
        }
        if(data != null && _channel.isWritable()){
            if(cb != null){
                _channel.writeAndFlush(data).addListener(future -> {
                    try {
                        if(future.isSuccess()){
                            cb.onCallback(null);
                        }else {
                            cb.onCallback(future.cause().toString());
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                });
            }else{
                _channel.writeAndFlush(data);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean writeThenClose(Object dataObj) {
        Object data = null;
        if(dataObj instanceof IoDataOutWrap){
            IoDataOutWrap dataWrap = (IoDataOutWrap)dataObj;
            data = dataWrap.getData();
        }else{
            data = dataObj;
        }
        //
        if(data != null && _channel.isWritable()){
            _channel.writeAndFlush(data).addListener(ChannelFutureListener.CLOSE);
            return true;
        }
        return false;
    }

    @Override
    public boolean writeToBuffer(Object dataObj) {
        Object data;
        if(dataObj instanceof IoDataOutWrap){
            IoDataOutWrap dataWrap = (IoDataOutWrap)dataObj;
            data = dataWrap.getData();
        }else{
            data = dataObj;
        }
        if(data != null && _channel.isWritable()){
            _channel.write(data);
            return true;
        }
        return false;
    }

    @Override
    public boolean flushBuffer() {
        _channel.flush();
        return true;
    }

    @Override
    public boolean isWritable() {
        return _channel.isWritable();
    }

    public int release(){
        int cnt = _refCnt.decrementAndGet();
        if(cnt == 0){
            _doClose();
        }
        return cnt;
    }
    public int retain(){
        return _refCnt.incrementAndGet();
    }

    public boolean onReadData(Object data){
        if(_cbReadFull != null){
            try {
                _cbReadFull.onRead(data, this);
            }catch (Throwable e){
                e.printStackTrace();
            }
            return true;
        }
        else if(_cbRead != null){
            try {
                _cbRead.onRead(data);
            }catch (Throwable e){
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
    public void onConnGone(){
        if(_cbGoneFull != null){
            _cbGoneFull.onCallback(this);
        }
        else if(_cbGone != null){
            _cbGone.onCallback();
        }
    }

    public void onChannelError(String error){
        if(_cbErrorFull != null){
            _cbErrorFull.onCallback(error, this);
        }
        else if(_cbError != null){
            _cbError.onCallback(error);
        }
    }

    public void onHandshakeDone(){
        if(_cbHandshakeDoneFull != null){
            _cbHandshakeDoneFull.onCallback(this);
        }
        else if(_cbHandshakeDone != null){
            _cbHandshakeDone.onCallback();
        }
    }

    private LoopWorker _checkBind4Read(String method) throws StatusIllegalException {
        LoopWorker worker = _checkWorkerValid(method);
        _checkBindWorkerRead(worker.pid);
        return worker;
    }
    private LoopWorker _checkWorkerValid(String method) throws StatusIllegalException {
        LoopWorker worker = LoopWorker.getThlWorker();
        if(worker == null){  // exception, not be called in worker thread
            throw new StatusIllegalException(method+" must be called in workerThread, not in curThread: " + Thread.currentThread().getName());
        }
        return worker;
    }
    private synchronized void _checkBindWorkerRead(long curWorkerId) throws StatusIllegalException {
        if(_bindWorker4Read == 0){   // 1st bind
            _bindWorker4Read = curWorkerId;
        }else if(_bindWorker4Read != curWorkerId){  // has bind in another thread
            throw new StatusIllegalException("current TcpChannel has be started in worker("+_bindWorker4Read+"), curWorker is "+curWorkerId);
        }
    }

    @Override
    public ChannelSender sender() {
        if(_senderWrap == null){
            _senderWrap = new ChannelSenderWrap(_netWrap, this);
        }
        return _senderWrap;
    }
}
