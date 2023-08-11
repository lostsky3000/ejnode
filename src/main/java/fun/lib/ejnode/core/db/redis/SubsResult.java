package fun.lib.ejnode.core.db.redis;

public interface SubsResult {
    void onCallback(String error, String channel, int subsNum);
}
