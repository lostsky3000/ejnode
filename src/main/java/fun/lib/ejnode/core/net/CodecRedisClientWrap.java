package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.CodecRedisClient;

public final class CodecRedisClientWrap implements CodecRedisClient {

    public int cacheMax;

    public CodecRedisClientWrap(){
        _initDefault();
    }


    @Override
    public int codecType() {
        return TYPE_REDIS_CLIENT;
    }

    private void _initDefault(){
        cacheMax = 1024*64;
    }

    @Override
    public CodecRedisClient cacheMax(int maxLen) {
        cacheMax = maxLen;
        return this;
    }
}
