package fun.lib.ejnode.api.net;

public interface ChannelWorkerChooser {
    long next(TcpChannel channel);
}
