package fun.lib.ejnode.core;

import fun.lib.ejnode.api.Logger;

public final class EJLoggerWrap implements Logger {

    private EJLoggerCore _logger;
    private MsgLog _msgLog;

    protected EJLoggerWrap(){
        EJNode ejNode = EJNode.get();
        _logger = ejNode.getLogger();
    }

    @Override
    public void trace(String str) {
        trace(null, str);
    }

    @Override
    public void trace(String tag, String str) {
        _doBatchLog(tag, str, LEVEL_TRACE);
    }

    @Override
    public void debug(String str) {
        debug(null, str);
    }

    @Override
    public void debug(String tag, String str) {
        _doBatchLog(tag, str, LEVEL_DEBUG);
    }

    @Override
    public void info(String str) {
        info(null, str);
    }

    @Override
    public void info(String tag, String str) {
        _doBatchLog(tag, str, LEVEL_INFO);
    }

    @Override
    public void warn(String str) {
        warn(null, str);
    }

    @Override
    public void warn(String tag, String str) {
        _doBatchLog(tag, str, LEVEL_WARN);
    }

    @Override
    public void error(String str) {
        error(null, str);
    }

    @Override
    public void error(String tag, String str) {
        _doBatchLog(tag, str, LEVEL_ERROR);
    }

    @Override
    public void fatal(String str) {
        fatal(null, str);
    }

    @Override
    public void fatal(String tag, String str) {
        _doBatchLog(tag, str, LEVEL_FATAL);
    }

    private void _doBatchLog(String tag, String str, int level){
        if(_msgLog == null){
            _msgLog = _logger.newMsgLog();
        }
        _msgLog.addItem(tag, str, level);
    }

    protected void onLogBatchEnd(){
        if(_msgLog != null){
            _logger.addLogMsg(_msgLog);
            _msgLog = null;
        }
    }
}
