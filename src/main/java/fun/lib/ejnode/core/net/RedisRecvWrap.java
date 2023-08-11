package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.RedisRecv;

public final class RedisRecvWrap extends RedisRecv {

    public final Object data;

    public RedisRecvWrap(Object data){
        this.data = data;
    }

    @Override
    public Object rawData() {
        return data;
    }

}
