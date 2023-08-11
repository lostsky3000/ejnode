package fun.lib.ejnode.core;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public final class EJIoMgr implements EJNodeLife{

    protected EJIoMgr(){

    }

    private EventLoopGroup _ioGrpBoss;
    private EventLoopGroup _ioGrpWorkerOutter;
    private EventLoopGroup _ioGrpWorkerInner;
    protected void start(int thBossNum, int thOutterNum, int thInnerNum){
        if(EJEnvWrap.isLinux()){
            _ioGrpBoss = new EpollEventLoopGroup(thBossNum);
            _ioGrpWorkerOutter = new EpollEventLoopGroup(thOutterNum);
            _ioGrpWorkerInner = new EpollEventLoopGroup(thInnerNum);
        }else{
            _ioGrpBoss = new NioEventLoopGroup(thBossNum);
            _ioGrpWorkerOutter = new NioEventLoopGroup(thOutterNum);
            _ioGrpWorkerInner = new NioEventLoopGroup(thInnerNum);
        }
    }

    protected void shutdown(){
//        if(!_hasShutdown.getAndSet(true)){
//            _ioGrpBoss.shutdownGracefully();
//            _ioGrpWorkerOutter.shutdownGracefully();
//            _ioGrpWorkerInner.shutdownGracefully();
//        }
    }

    public EventLoopGroup ioGroupBoss(){
        return _ioGrpBoss;
    }
    public EventLoopGroup ioGroupWorkerInner(){
        return _ioGrpWorkerInner;
    }
    public EventLoopGroup ioGroupWorkerOutter(){
        return _ioGrpWorkerOutter;
    }

    @Override
    public void onNodeExit() {
        _ioGrpBoss.shutdownGracefully();
        _ioGrpWorkerOutter.shutdownGracefully();
        _ioGrpWorkerInner.shutdownGracefully();
    }
}
