package fun.lib.ejnode.core.db.redis;

import fun.lib.ejnode.api.callback.CommonFuture;

public interface SubsFuture extends CommonFuture {
    SubsFuture onSubsResult(SubsResult cb);
    SubsFuture onMessage(SubMessage cb);
}
