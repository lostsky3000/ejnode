package fun.lib.ejnode.core.db.mysql;

import fun.lib.ejnode.api.StatusIllegalException;
import fun.lib.ejnode.api.net.TcpChannel;
import fun.lib.ejnode.core.db.redis.RedisClientWrap;
import fun.lib.ejnode.core.db.redis.RedisCmd;
import fun.lib.ejnode.core.net.ClientCodec;
import fun.lib.ejnode.core.pool.ClientPoolCore;
import fun.lib.ejnode.core.pool.PoolClient;

public final class MysqlClientWrap extends PoolClient {

    private ClientPoolCore _pool = null;

    private TcpChannel _channel;
    private boolean _connCalled;

    protected MysqlClientWrap(long id, ClientPoolCore pool) {
        super(id);
        _pool = pool;
    }

    @Override
    public void connect(long connTimeout) {
        if(_connCalled){
            return;
        }
        _connCalled = true;
        //
        _pool.net().createClient(_pool.getHost(), _pool.getPort())
                .codec(ClientCodec.mysql())
                .timeout(connTimeout)
                .connect((error, channel) -> {
                    if(error != null){   // conn error
                        _pool.onClientConnDone(this, error);
                        return;
                    }
                    try {
                        channel.onRead(data -> {
                            _procReadData(data);
                        }).onError(error1 -> {
//                            _poolWrap.onClientDecodeError(this, error1);
                        }).onClose(()->{
                            _channel = null;
                            _pool.onClientDisconn(this);
//                            System.out.println("redis client closed by server");
                        });
                    } catch (StatusIllegalException e) {
                        e.printStackTrace();
                    }
                    _channel = channel;

//                    RedisClientWrap.RspCb<String> rsp = _doSendCommand(_apiVerCnt, RedisCmd.AUTH, pwd);
//                    rsp.onResult((error1, result) -> {
//                        if(error1 == null && result.equals("OK")){
//                            _updateApi();
//                            _pool.onClientConnDone(this, null);
//                        }else{
//                            _pool.onClientConnDone(this, error1!=null?error1:"auth failed: "+result);
//                            channel.close();
//                        }
//                    });
//                    _channel.flushBuffer();
                });
    }

    private void _procReadData(Object raw){

    }

    @Override
    public void close() {
        if(_channel != null){
            _channel.close();
            _channel = null;
        }
    }

    @Override
    public boolean isAlive() {
        return _channel != null;
    }
}
