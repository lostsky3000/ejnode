package fun.lib.ejnode.core.db.redis;

public interface SubMessage {
    void onCallback(String channel, String msg);
}
