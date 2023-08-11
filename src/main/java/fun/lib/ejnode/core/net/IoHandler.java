package fun.lib.ejnode.core.net;

import fun.lib.ejnode.core.EJNetWrap;
import io.netty.channel.Channel;

public interface IoHandler {

    Channel getChannel();

    void onReadStart(EJNetWrap netWrap, long channelId);
}
