package fun.lib.ejnode.core.net;

import fun.lib.ejnode.api.net.CodecMysqlClient;

public class CodecMysqlClientWrap implements CodecMysqlClient {

    public CodecMysqlClientWrap(){
        _initDefault();
    }

    @Override
    public int codecType() {
        return TYPE_MYSQL_CLIENT;
    }

    private void _initDefault(){

    }

}
