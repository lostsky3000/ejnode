package fun.lib.ejnode.api.net;

import fun.lib.ejnode.api.Net;
import fun.lib.ejnode.api.callback.CbCommonResult;

public class CommonTcpServer {

    protected final Net net;
    protected TcpServerChannel svrChannel;
    protected CbCommonResult cbListenRet;
    protected boolean closeCalled;

    public CommonTcpServer(Net net){
        this.net = net;
        closeCalled = false;
    }

    public void close(){
        if(closeCalled){
            return;
        }
        closeCalled = true;
        if(svrChannel != null){
            svrChannel.close();
            svrChannel = null;
        }
    }

    protected void procListenResult(String error, TcpServerChannel channel){
        if(closeCalled){
            if(channel != null){
                channel.close();
            }
        }else{
            svrChannel = error==null?channel:null;
            if(cbListenRet != null){
                try {
                    cbListenRet.onCallback(error);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }
    }
}
