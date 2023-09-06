package fun.lib.ejnode.core;

public final class ForkParams {

    private Object _userData;
    private int _ioServerGroupThreadNum;
    private int _ioClientGroupThreadNum;

    protected ForkParams(Object userData, int ioServerGroupThreadNum, int ioClientGroupThreadNum){
        _userData = userData;
        _ioServerGroupThreadNum = ioServerGroupThreadNum;
        _ioClientGroupThreadNum = ioClientGroupThreadNum;
    }

    public Object userData(){
        return _userData;
    }

    public int ioServerGroupThreadNum(){
        return _ioServerGroupThreadNum;
    }

    public int ioClientGroupThreadNum(){
        return _ioClientGroupThreadNum;
    }
}
