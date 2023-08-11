package fun.lib.ejnode.api;

import fun.lib.ejnode.api.callback.*;

public interface Timer {

    TimerFuture nextTick(Object userData, CbCommonUD cb);
    TimerFuture nextTick(CbCommon cb);

    TimerFuture immediate(Object userData, CbCommonUD cb);
    TimerFuture immediate(CbCommon cb);

    TimerFuture timeout(long delay, Object userData, CbCommonUD cb);
    TimerFuture timeout(long delay, CbCommon cb);

    TimerFuture schedule(long interval, Object userData, CbScheduleUD cb);
    TimerFuture schedule(long interval, CbSchedule cb);

}
