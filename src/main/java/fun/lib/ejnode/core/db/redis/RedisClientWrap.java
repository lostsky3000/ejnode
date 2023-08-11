package fun.lib.ejnode.core.db.redis;

import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.api.callback.CbCommon;
import fun.lib.ejnode.api.net.TcpChannel;
import fun.lib.ejnode.core.net.ClientCodec;
import fun.lib.ejnode.util.container.DFPooledLinkedList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.List;

public final class RedisClientWrap{

    private RedisPoolWrap _poolWrap = null;

    private long _keepAliveInterval = 1000;

    protected final long id;
    protected final String host;
    protected final int port;
    protected final String pwd;

    private TcpChannel _channel;
    private boolean _connCalled;
    private byte[] _bytesBulkLen;

    private DFPooledLinkedList<RspCb> _lsRspCb;
    private HashMap<String, SubsCtx> _mapPubSubCtx;
    private int _actSubsChannelNum;
    private ClientApi _api;
    private long _apiVerCnt;

    public RedisClientWrap(){   // debug
        id = 0;
        _poolWrap = null;
        host = null;
        port = 0;
        pwd = null;
        _bytesBulkLen = new byte[32];
        _bytesBulkLen[0] = '$';
        _lsRspCb = new DFPooledLinkedList<>();
    }

    protected RedisClientWrap(long id, RedisPoolWrap poolWrap, String host, int port, String pwd){
        this.id = id;
        _poolWrap = poolWrap;
        _keepAliveInterval = poolWrap.keepAliveInterval;
        this.host = host;
        this.port = port;
        this.pwd = pwd;
        _bytesBulkLen = new byte[32];
        _bytesBulkLen[0] = '$';
        _lsRspCb = new DFPooledLinkedList<>();
        //
        _apiVerCnt = 0;
        _connCalled = false;
        _mapPubSubCtx = new HashMap<>();
        _actSubsChannelNum = 0;
    }

    protected void connect(long timeout){
        if(_connCalled){
            return;
        }
        _connCalled = true;
        _poolWrap.net.createClient(host, port)
                .codec(ClientCodec.redis())
                .timeout(timeout)
                .connect((error, channel) -> {
                    if(error != null){   // conn error
                        _poolWrap.onClientConnDone(this, error);
                        return;
                    }
                    try {
                        channel.onRead(data -> {
                            _procReadData(data);
                        }).onError(error1 -> {
                            _poolWrap.onClientDecodeError(this, error1);
                        }).onClose(()->{
                            _channel = null;
                            _poolWrap.onClientDisconn(this);
//                            System.out.println("redis client closed by server");
                        });
                    } catch (StatusIllegalException e) {
                        e.printStackTrace();
                    }
                    _channel = channel;
                    RspCb<String> rsp = _doSendCommand(_apiVerCnt, RedisCmd.AUTH, pwd);
                    rsp.onResult((error1, result) -> {
                        if(error1 == null && result.equals("OK")){
                            _updateApi();
                            _poolWrap.onClientConnDone(this, null);
                        }else{
                            _poolWrap.onClientConnDone(this, error1!=null?error1:"auth failed: "+result);
                            channel.close();
                        }
                    });
                    _channel.flushBuffer();
                });
    }
    protected void close(){
        if(_channel != null){
            _channel.close();
            _channel = null;
        }
    }
    private void _updateApi(){
        _api = new ClientApi(++_apiVerCnt, this);
    }
    protected RedisClient getApi(){
        return _api;
    }

    private long _tmLastConn = 0;
    private boolean _onKeepAliveCheck = false;
    private void _checkKeepAlive(long delay){
        if(_onKeepAliveCheck){
            return;
        }
        _onKeepAliveCheck = true;
        _poolWrap.timer.timeout(delay, _cbKeepAlive);
    }
    private final CbCommon _cbKeepAlive = () -> {
        _onKeepAliveCheck = false;
        if(_channel == null){
            return;
        }
        long tmNow = System.currentTimeMillis();
        long dlt = _keepAliveInterval - (tmNow - _tmLastConn);
        if(dlt < 100){
//            _poolWrap.log.debug("keepAlive ping");
            _doSendCommand(0, true, _apiVerCnt, RedisCmd.PING, null);
        }else{
            _checkKeepAlive(dlt);
        }
    };

    private RspCb _curRspCbSubs;
    private RspCb _curRspCbUnsubs;
    public void _procReadData(Object data){
        _tmLastConn = System.currentTimeMillis();
        _checkKeepAlive(_keepAliveInterval);
        //
        RspCb rspCb = null;
        if(_curRspCbSubs != null){
            _procSubsChannel((List<Object>) data, _curRspCbSubs);
            if(--_curRspCbSubs.channelCnt == 0){
                _curRspCbSubs = null;
            }
        }
        else if(_curRspCbUnsubs != null){
            if(_curRspCbUnsubs.channelCnt < 0){   // unsubs all channel
                int subsNum = _procUnsubsChannel((List<Object>) data, _curRspCbUnsubs);
                if(subsNum < 1){
                    _curRspCbUnsubs = null;
                }
            }else{  // unsubs specified channels
                _procUnsubsChannel((List<Object>) data, _curRspCbUnsubs);
                if(--_curRspCbUnsubs.channelCnt == 0){
                    _curRspCbUnsubs = null;
                }
            }
        }
        else if(_mapPubSubCtx.isEmpty()){    // not in subscribe state
            rspCb = _lsRspCb.poll();
        }
        else{  // in subscribe state
            boolean notPushMsg = true;
            if(data instanceof List){
                List<Object> ls = (List<Object>) data;
                String type = (String) ls.get(0);
                if(type.equals("message")){   // msg of publish
                    notPushMsg = false;
                    String channel = (String) ls.get(1);
                    _procSubsMsg(channel, (String)ls.get(2));
                }
            }
            if(notPushMsg){
                rspCb = _lsRspCb.poll();
            }
        }
        if(rspCb != null){
            rspCb.done = true;
            if(data instanceof Exception){
                rspCb.error = data.toString();
                rspCb.succ = false;
                rspCb.result = null;
            }else{
                rspCb.error = null;
                rspCb.succ = true;
                rspCb.result = data;
            }
            int pubSubFlag = rspCb.pubSubFlag;
            if(pubSubFlag > 0){  // pubSub relative
                if(pubSubFlag == 1){   // subscribe rsp
                    if(data instanceof List){   // subscribe rsp(succ)
                        _procSubsChannel((List<Object>) data, rspCb);
                        if(--rspCb.channelCnt > 0){
                            _curRspCbSubs = rspCb;
                        }
                    }else{   // Exception, subscribe failed
                        SubsResult cbRet = rspCb.cbSubsRet;
                        if(cbRet != null){
                            try {
                                cbRet.onCallback(data.toString(), null, 0);
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    }
                }else if(pubSubFlag == 2){   // unsubscribe
                    int subsNum = _procUnsubsChannel((List<Object>) data, rspCb);
                    if(rspCb.channelCnt < 0){   // unsubs all channels
                        if(subsNum > 0){
                            _curRspCbUnsubs = rspCb;
                        }
                    }else{   // unsubs specified channels
                        if(--rspCb.channelCnt > 0){
                            _curRspCbUnsubs = rspCb;
                        }
                    }
                }
            }else{   // common req-rsp
                CommandResult cb = rspCb.cbRsp;
                if(cb != null){
                    try {
                        cb.onResult(rspCb.error, rspCb.result);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void _procSubsMsg(String channel, String msg){
        SubsCtx ctx = _mapPubSubCtx.get(channel);
        assert ctx != null;
        SubMessage cb = ctx.cbMsg;
        if(cb != null){
            try {
                cb.onCallback(channel, msg);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }
    private void _procSubsChannel(List<Object> ls, RspCb rspCb){
        String channel = (String)ls.get(1);
        int subsNum = (int)((long)ls.get(2));
        if(_mapPubSubCtx.isEmpty()){
            if(_channel != null){
                _channel.flushBuffer();
            }
        }
        SubsCtx ctx = new SubsCtx(channel, rspCb.cbSubsMsg);
        _mapPubSubCtx.put(channel, ctx);
        SubsResult cbRet = rspCb.cbSubsRet;
        if(cbRet != null){
            try {
                cbRet.onCallback(null, channel, subsNum);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }
    private int _procUnsubsChannel(List<Object> ls, RspCb rspCb){
        String channel = null;
        Object objChannel = ls.get(1);
        if(objChannel != null){
            channel = (String) objChannel;
        }
        int subsNum = (int)((long)ls.get(2));
        if(channel != null){
            _mapPubSubCtx.remove(channel);
        }
        SubsResult cb = rspCb.cbUnsubsRet;
        if(cb != null){
            try {
                cb.onCallback(null, channel, subsNum);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
        return subsNum;
    }

    //
    protected <T> RspCb<T> _doSendCommand(long verCaller, int cmd, String key, String... args){
        return _doSendCommand(0, false, verCaller, cmd, key, args);
    }
    protected <T> RspCb<T> _doSendCommand(int pubSubFlag, boolean flush, long verCaller, int cmd, String key, String... args){
        RspCb<T> rspCb = new RspCb<>(pubSubFlag);
        if(verCaller != _apiVerCnt){  // version not match
            _procSendError(rspCb, "client has released");
            return rspCb;
        }
        if(_channel == null){
            _procSendError(rspCb, "connection has gone(1)");
            return rspCb;
        }

//        if(pubSubFlag == 1){
//            ++_subsCalledCnt;
//        }

        if(_mapPubSubCtx.size() > 0){  // in subscribe state, send command immediately
            flush = true;
        }
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(128);
        int argNumOri = 0;
        try {
            // write array head
            argNumOri = Math.min( key!=null?1+args.length:args.length, RedisCmd.CMD_LEN_TEMPLATE_MAX - 1 );
            int argNum = argNumOri;
            int bytesCmd = RedisCmd.getCmdLenTemplate(1 + argNum);
            buf.writeBytes(RedisCmd.CMD_LEN_TEMPLATE, bytesCmd>>8, bytesCmd&0xff);  // `*xx\r\n`
            //  write cmd
            bytesCmd = RedisCmd.getCmdTemplate(cmd);
            buf.writeBytes(RedisCmd.CMD_TEMPLATE, bytesCmd>>8, bytesCmd&0xff);  // `$4\r\nAUTH\r\n`
            // write args
            if(argNum > 0){
                if(key != null){
                    _writeArg(buf, key);
                    --argNum;
                }
                if(argNum > 0){
                    for(int i=0; i<argNum; ++i){
                        _writeArg(buf, args[i]);
                    }
                }
            }
        }catch (Throwable e){
            buf.release();
            _procSendError(rspCb, e.toString());
            e.printStackTrace();
            return rspCb;
        }
//        String str = (String) buf.getCharSequence(buf.readerIndex(), buf.readableBytes(), CharsetUtil.UTF_8);  // debug

        if(flush?_channel.write(buf):_channel.writeToBuffer(buf)){
            _lsRspCb.offer(rspCb);
            if(pubSubFlag > 0){    // pubSub command
                if(pubSubFlag == 1){   // subscribe
                    rspCb.channelCnt = argNumOri;
                }else if(pubSubFlag == 2){   // unsubscribe
                    rspCb.channelCnt = argNumOri>0?argNumOri:-1;
                }
            }
            return rspCb;
        }else {
            buf.release();
        }
        _procSendError(rspCb, "connection has gone(2)");
        return rspCb;
    }

    private<T> void _procSendError(RspCb<T> rsp, String error){
        rsp.done = true;
        rsp.succ = false;
        rsp.error = error;
        _poolWrap.timer.nextTick(()->{
            int pubSubFlag = rsp.pubSubFlag;
            if(pubSubFlag > 0){   // pubSubRelative
                if(pubSubFlag == 1){   // subscribe
                    SubsResult cb = rsp.cbSubsRet;
                    if(cb != null){
                        try {
                            cb.onCallback(error, null, 0);
                        }catch (Throwable e){
                            e.printStackTrace();
                        }
                    }
                }else if(pubSubFlag == 2){  // unsubscribe

                }
            }else {
                CommandResult cb = rsp.cbRsp;
                if(cb != null){
                    try {
                        cb.onResult(rsp.error, null);
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void _apiRelease(long verCaller){
        if(verCaller != _apiVerCnt){   // version not match
            return;
        }
        if(_channel != null){   // flush buffered data
            _channel.flushBuffer();
        }
        _updateApi();
        //
        _poolWrap.onClientRelease(this);
    }

    protected boolean isAlive(){
        return _channel != null;
    }

    private void _writeArg(ByteBuf buf, String arg){
        byte[] tmpBytes = arg.getBytes(CharsetUtil.UTF_8);
        int bulkLen = tmpBytes.length;
        if(bulkLen < 10){
            _bytesBulkLen[1] = (byte) (bulkLen + '0');
            buf.writeBytes(_bytesBulkLen, 0, 2);
        }else if(bulkLen < 100){
            _bytesBulkLen[1] = (byte) (bulkLen/10 + '0');
            _bytesBulkLen[2] = (byte) (bulkLen%10 + '0');
            buf.writeBytes(_bytesBulkLen, 0, 3);
        }else {
            int idx = _bytesBulkLen.length - 1;
            int len = 0;
            while(bulkLen > 0){
                ++len;
                _bytesBulkLen[idx--] = (byte) (bulkLen%10);
                bulkLen /= 10;
            }
            _bytesBulkLen[idx] = '$';
            buf.writeBytes(_bytesBulkLen, idx, len);
        }
        buf.writeBytes(BYTES_CRLF);
        buf.writeBytes(tmpBytes);
        buf.writeBytes(BYTES_CRLF);
    }

    static class RspCb<T> implements CommandFuture<T>, SubsFuture, UnsubsFuture {
        protected boolean done;
        protected boolean succ;
        protected String error;
        protected T result;
        protected CommandResult<T> cbRsp;
        protected SubsResult cbSubsRet;
        protected SubsResult cbUnsubsRet;
        protected SubMessage cbSubsMsg;
        protected int pubSubFlag;

        protected int channelCnt;

        private RspCb(int pubSubFlag){
            this.pubSubFlag = pubSubFlag;
        }

        // CommonFuture
        @Override
        public boolean isDone() {
            return done;
        }
        @Override
        public boolean isSucc() {
            return succ;
        }
        @Override
        public String error() {
            return error;
        }
        // CommandFuture
        @Override
        public T getResult() {
            return result;
        }
        @Override
        public void onResult(CommandResult<T> listener) {
            cbRsp = listener;
        }
        // SubFuture
        @Override
        public SubsFuture onSubsResult(SubsResult listener) {
            cbSubsRet = listener;
            return this;
        }
        @Override
        public SubsFuture onMessage(SubMessage listener) {
            cbSubsMsg = listener;
            return this;
        }

        @Override
        public UnsubsFuture onUnsubsResult(SubsResult listener) {
            cbUnsubsRet = listener;
            return this;
        }
        // UnsubFuture

    }
    static class SubsCtx {
        protected String channel;
        protected SubMessage cbMsg;
        protected SubsCtx(String channel, SubMessage cbMsg){
            this.channel = channel;
            this.cbMsg = cbMsg;
        }
    }

    private static final byte[] BYTES_CRLF = {'\r','\n'};

    static class ClientApi implements RedisClient{
        protected final long version;
        protected RedisClientWrap _core;
        //
        private RedisApiHash _apiHash;
        private RedisApiList _apiList;
        private RedisApiSet _apiSet;
        private RedisApiZSet _apiZSet;
        private RedisApiScript _apiScript;
        //
        private ClientApi(long version, RedisClientWrap core){
            this.version = version;
            _core = core;
        }
        @Override
        public void release() {
            _core._apiRelease(version);
//            _core = null; // ??
        }
        // command
//        @Override
//        public CommandFuture<String> auth(String password){
//            return _core._doSendCommand(version, RedisCmd.AUTH, password);
//        }

        @Override
        public CommandFuture<String> get(String key){
            return _core._doSendCommand(version, RedisCmd.GET, key);
        }
        @Override
        public CommandFuture<String> set(String key, String value){
            return _core._doSendCommand(version, RedisCmd.SET, key, value);
        }
        @Override
        public CommandFuture<Long> del(String... keys) {
            return _core._doSendCommand(version, RedisCmd.DEL, null, keys);
        }
        @Override
        public CommandFuture<Object> ping() {
            return _core._doSendCommand(version, RedisCmd.PING, null);
        }
        @Override
        public CommandFuture<String> info() {
            return _core._doSendCommand(version, RedisCmd.INFO, null);
        }
        @Override
        public CommandFuture<Long> decr(String key) {
            return _core._doSendCommand(version, RedisCmd.DECR, key);
        }
        @Override
        public CommandFuture<Long> decrBy(String key, long decrement) {
            return _core._doSendCommand(version, RedisCmd.DECRBY, key, decrement+"");
        }
        @Override
        public CommandFuture<Long> incr(String key) {
            return _core._doSendCommand(version, RedisCmd.INCR, key);
        }
        @Override
        public CommandFuture<Long> incrBy(String key, long increment) {
            return _core._doSendCommand(version, RedisCmd.INCRBY, key, increment+"");
        }
        @Override
        public CommandFuture<String> incrByFloat(String key, float increment) {
            return _core._doSendCommand(version, RedisCmd.INCRBYFLOAT, key, increment+"");
        }
        @Override
        public CommandFuture<String> echo(String msg) {
            return _core._doSendCommand(version, RedisCmd.ECHO, msg);
        }
        @Override
        public CommandFuture<Long> exists(String... keys) {
            return _core._doSendCommand(version, RedisCmd.EXISTS, null, keys);
        }
        @Override
        public CommandFuture<Long> expire(String key, long seconds) {
            return _core._doSendCommand(version, RedisCmd.EXPIRE, key, seconds+"");
        }
        @Override
        public CommandFuture<Long> expireAt(String key, long timestamp) {
            return _core._doSendCommand(version, RedisCmd.EXPIREAT, key, timestamp+"");
        }
        // since 7.0.0
        @Override
        public CommandFuture<Long> expireTime(String key) {
            return _core._doSendCommand(version, RedisCmd.EXPIRETIME, key);
        }
        // since 6.2.0
        @Override
        public CommandFuture<String> getDel(String key) {
            return _core._doSendCommand(version, RedisCmd.GETDEL, key);
        }
        @Override
        public CommandFuture<String> getRange(String key, int start, int end) {
            return _core._doSendCommand(version, RedisCmd.GETRANGE, key, start+"", end+"");
        }
        @Override
        public CommandFuture<String> getSet(String key, String value) {
            return _core._doSendCommand(version, RedisCmd.GETSET, key, value);
        }
        @Override
        public CommandFuture<List<Object>> mGet(String... keys) {
            return _core._doSendCommand(version, RedisCmd.MGET, null, keys);
        }
        @Override
        public CommandFuture<String> mSet(String... pairs) {
            return _core._doSendCommand(version, RedisCmd.MSET, null, pairs);
        }
        @Override
        public CommandFuture<Long> mSetNX(String... pairs) {
            return _core._doSendCommand(version, RedisCmd.MSETNX, null, pairs);
        }
        @Override
        public CommandFuture<String> randomKey() {
            return _core._doSendCommand(version, RedisCmd.RANDOMKEY, null);
        }
        @Override
        public CommandFuture<String> rename(String key, String newKey) {
            return _core._doSendCommand(version, RedisCmd.RENAME, key, newKey);
        }
        @Override
        public CommandFuture<Long> renameNX(String key, String newKey) {
            return _core._doSendCommand(version, RedisCmd.RENAMENX, key, newKey);
        }
        @Override
        public CommandFuture<Long> strLen(String key) {
            return _core._doSendCommand(version, RedisCmd.STRLEN, key);
        }
        @Override
        public CommandFuture<List<Object>> time() {
            return _core._doSendCommand(version, RedisCmd.TIME, null);
        }
        @Override
        public CommandFuture<Long> touch(String... keys) {
            return _core._doSendCommand(version, RedisCmd.TOUCH, null, keys);
        }
        @Override
        public CommandFuture<Long> ttl(String key) {
            return _core._doSendCommand(version, RedisCmd.TTL, key);
        }
        @Override
        public CommandFuture<String> type(String key) {
            return _core._doSendCommand(version, RedisCmd.TYPE, key);
        }
        @Override
        public CommandFuture<Long> unlink(String... keys) {
            return _core._doSendCommand(version, RedisCmd.UNLINK, null, keys);
        }
        @Override
        public CommandFuture<Long> append(String key, String value) {
            return _core._doSendCommand(version, RedisCmd.APPEND, key, value);
        }
        @Override
        public CommandFuture<String> getEX(String key, String... args) {
            return _core._doSendCommand(version, RedisCmd.GETEX, key, args);
        }
        @Override
        public CommandFuture<Long> setRange(String key, int offset, String value) {
            return _core._doSendCommand(version, RedisCmd.SETRANGE, key, offset+"", value);
        }
        @Override
        public CommandFuture<String> select(int index) {
            return _core._doSendCommand(version, RedisCmd.SELECT, index+"");
        }
        @Override
        public SubsFuture subscribe(String channel, String... channels) {
            return _core._doSendCommand(1, true, version, RedisCmd.SUBSCRIBE, channel, channels);
        }
        @Override
        public UnsubsFuture unsubscribe(String... channels) {
            return _core._doSendCommand(2, true, version, RedisCmd.UNSUBSCRIBE, null, channels);
        }

        @Override
        public SubsFuture psubscribe(String... channelPatterns) {
            _core._doSendCommand(version, RedisCmd.PSUBSCRIBE, null, channelPatterns);
            return null;
        }
        @Override
        public CommandFuture<Long> publish(String channel, String msg) {
            return _core._doSendCommand(version, RedisCmd.PUBLISH, channel, msg);
        }

        @Override
        public RedisApiHash apiHash() {
            if(_apiHash == null){
                _apiHash = new RedisApiHash(_core, version);
            }
            return _apiHash;
        }

        @Override
        public RedisApiList apiList() {
            if(_apiList == null){
                _apiList = new RedisApiList(_core, version);
            }
            return _apiList;
        }

        @Override
        public RedisApiSet apiSet() {
            if(_apiSet == null){
                _apiSet = new RedisApiSet(_core, version);
            }
            return _apiSet;
        }

        @Override
        public RedisApiZSet apiZSet() {
            if(_apiZSet == null){
                _apiZSet = new RedisApiZSet(_core, version);
            }
            return _apiZSet;
        }

        @Override
        public RedisApiScript apiScript() {
            if(_apiScript == null){
                _apiScript = new RedisApiScript(_core, version);
            }
            return _apiScript;
        }

    }


}
