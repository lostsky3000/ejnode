package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.SslCfgClient;
import fun.lib.ejnode.api.net.TcpClient;

public final class SslCfgCliWrap implements SslCfgClient<TcpClient> {

    private final TcpClientWrap _clientWrap;

    protected SslCfgCliWrap(TcpClientWrap clientWrap){
        _clientWrap = clientWrap;
    }

    @Override
    public TcpClient end() {
        return _clientWrap;
    }

    private void _initDefault(){

    }
}
