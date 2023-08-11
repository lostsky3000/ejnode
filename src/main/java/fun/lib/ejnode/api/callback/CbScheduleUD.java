package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.TimerFuture;

public interface CbScheduleUD {
    void onSchedule(TimerFuture task, Object userData);
}
