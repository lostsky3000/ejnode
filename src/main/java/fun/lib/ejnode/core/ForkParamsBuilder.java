package fun.lib.ejnode.core;

public final class ForkParamsBuilder {

    protected ForkParamsBuilder(){
        _initDefault();
    }

    private void _initDefault(){
        _ioServerGroupThreadNum = 2;
        _ioClientGroupThreadNum = 1;
    }

    private Object _userData;
    public ForkParamsBuilder userData(Object userData){
        _userData = userData;
        return this;
    }

    private int _ioServerGroupThreadNum;
    public ForkParamsBuilder ioServerGroupThreadNum(int threadNum){
        _ioServerGroupThreadNum = Math.max(1, threadNum);
        return this;
    }

    private int _ioClientGroupThreadNum;
    public ForkParamsBuilder ioClientGroupThreadNum(int threadNum){
        _ioClientGroupThreadNum = Math.max(0, threadNum);
        return this;
    }

    public ForkParams build(){
        return new ForkParams(_userData, _ioServerGroupThreadNum, _ioClientGroupThreadNum);
    }
}
