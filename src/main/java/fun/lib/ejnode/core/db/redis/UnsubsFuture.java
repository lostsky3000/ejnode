package fun.lib.ejnode.core.db.redis;

import fun.lib.ejnode.api.callback.CommonFuture;

public interface UnsubsFuture extends CommonFuture {
    UnsubsFuture onUnsubsResult(SubsResult cb);
}
