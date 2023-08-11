package fun.lib.ejnode.api.net;

public interface CodecRedisClient extends Codec4Client {

    CodecRedisClient cacheMax(int maxLen);

}
