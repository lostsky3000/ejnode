package fun.lib.ejnode.api.callback;

import fun.lib.ejnode.api.TimerFuture;

public interface CbSchedule {

    void onSchedule(TimerFuture task);

}
