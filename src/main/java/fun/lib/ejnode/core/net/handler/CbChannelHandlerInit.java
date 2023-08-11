package fun.lib.ejnode.core.net.handler;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;


public interface CbChannelHandlerInit {

    void onHandlerInit(SocketChannel sockChannel, ChannelPipeline pipeline);

}
