package fun.lib.ejnode.core.db.redis;

import io.netty.util.CharsetUtil;

public final class RedisCmd {

    protected static int getCmdTemplate(int cmd){
        int idx = (cmd - 1) * CMD_TEMPLATE_LINE;
        if(idx < CMD_TEMPLATE.length){
            int tmpLen = CMD_TEMPLATE[idx++];
            return (idx<<8)|tmpLen;
        }
        return -1;
    }

    protected static int getCmdLenTemplate(int len){
        int idx = (len - 1) * 10;
        if(idx < CMD_LEN_TEMPLATE.length){
            int tmpLen = CMD_LEN_TEMPLATE[idx++];
            return (idx<<8)|tmpLen;
        }
        return -1;
    }
    //
    // EVAL,EVALSHA,

    private static final int CMD_COMMON_BASE = 0;
    public static final int AUTH = CMD_COMMON_BASE + 1;
    public static final int GET = CMD_COMMON_BASE + 2;
    public static final int SET = CMD_COMMON_BASE + 3;
    public static final int DEL = CMD_COMMON_BASE + 4;
    public static final int PING = CMD_COMMON_BASE + 5;
    public static final int DECR = CMD_COMMON_BASE + 6;
    public static final int DECRBY = CMD_COMMON_BASE + 7;
    public static final int ECHO = CMD_COMMON_BASE + 8;
    public static final int EXISTS = CMD_COMMON_BASE + 9;
    public static final int EXPIRE = CMD_COMMON_BASE + 10;
    public static final int EXPIREAT = CMD_COMMON_BASE + 11;
    public static final int EXPIRETIME = CMD_COMMON_BASE + 12;
    public static final int GETDEL = CMD_COMMON_BASE + 13;
    public static final int GETRANGE = CMD_COMMON_BASE + 14;
    public static final int GETSET = CMD_COMMON_BASE + 15;
    public static final int INCR = CMD_COMMON_BASE + 16;
    public static final int INCRBY = CMD_COMMON_BASE + 17;
    public static final int INCRBYFLOAT = CMD_COMMON_BASE + 18;
    public static final int INFO = CMD_COMMON_BASE + 19;
    public static final int MGET = CMD_COMMON_BASE + 20;
    public static final int MSET = CMD_COMMON_BASE + 21;
    public static final int MSETNX = CMD_COMMON_BASE + 22;
    public static final int RANDOMKEY = CMD_COMMON_BASE + 23;
    public static final int RENAME = CMD_COMMON_BASE + 24;
    public static final int RENAMENX = CMD_COMMON_BASE + 25;
    public static final int STRLEN = CMD_COMMON_BASE + 26;
    public static final int TIME = CMD_COMMON_BASE + 27;
    public static final int TOUCH = CMD_COMMON_BASE + 28;
    public static final int TTL = CMD_COMMON_BASE + 29;
    public static final int TYPE = CMD_COMMON_BASE + 30;
    public static final int UNLINK = CMD_COMMON_BASE + 31;
    public static final int APPEND = CMD_COMMON_BASE + 32;
    public static final int GETEX = CMD_COMMON_BASE + 33;
    public static final int SETRANGE = CMD_COMMON_BASE + 34;
    public static final int SELECT = CMD_COMMON_BASE + 35;
    public static final int SUBSCRIBE = CMD_COMMON_BASE + 36;
    public static final int UNSUBSCRIBE = CMD_COMMON_BASE + 37;
    public static final int PSUBSCRIBE = CMD_COMMON_BASE + 38;
    public static final int PUBLISH = CMD_COMMON_BASE + 39;
    //
    private static final int CMD_HASH_BASE = 39;
    private static final int CMD_LIST_BASE = 54;
    private static final int CMD_SET_BASE = 69;
    private static final int CMD_ZSET_BASE = 85;
    private static final int CMD_SCRIPT_BASE = 114;
    // hash
    public static final int HDEL = CMD_HASH_BASE + 1;
    public static final int HEXISTS = CMD_HASH_BASE + 2;
    public static final int HGET = CMD_HASH_BASE + 3;
    public static final int HGETALL = CMD_HASH_BASE + 4;
    public static final int HINCRBY = CMD_HASH_BASE + 5;
    public static final int HINCRBYFLOAT = CMD_HASH_BASE + 6;
    public static final int HKEYS = CMD_HASH_BASE + 7;
    public static final int HLEN = CMD_HASH_BASE + 8;
    public static final int HMGET = CMD_HASH_BASE + 9;
    public static final int HMSET = CMD_HASH_BASE + 10;
    public static final int HRANDFIELD = CMD_HASH_BASE + 11;
    public static final int HSET = CMD_HASH_BASE + 12;
    public static final int HSETNX = CMD_HASH_BASE + 13;
    public static final int HSTRLEN = CMD_HASH_BASE + 14;
    public static final int HVALS = CMD_HASH_BASE + 15;
    // list
    public static final int LINDEX = CMD_LIST_BASE + 1;
    public static final int LINSERT = CMD_LIST_BASE + 2;
    public static final int LLEN = CMD_LIST_BASE + 3;
    public static final int LMOVE = CMD_LIST_BASE + 4;
    public static final int LPOP = CMD_LIST_BASE + 5;
    public static final int LPUSH = CMD_LIST_BASE + 6;
    public static final int LPUSHX = CMD_LIST_BASE + 7;
    public static final int LRANGE = CMD_LIST_BASE + 8;
    public static final int LREM = CMD_LIST_BASE + 9;
    public static final int LSET = CMD_LIST_BASE + 10;
    public static final int LTRIM = CMD_LIST_BASE + 11;
    public static final int RPOP = CMD_LIST_BASE + 12;
    public static final int RPOPLPUSH = CMD_LIST_BASE + 13;
    public static final int RPUSH = CMD_LIST_BASE + 14;
    public static final int RPUSHX = CMD_LIST_BASE + 15;
    // set
    public static final int SADD = CMD_SET_BASE + 1;
    public static final int SCARD = CMD_SET_BASE + 2;
    public static final int SDIFF = CMD_SET_BASE + 3;
    public static final int SDIFFSTORE = CMD_SET_BASE + 4;
    public static final int SINTER = CMD_SET_BASE + 5;
    public static final int SINTERSTORE = CMD_SET_BASE + 6;
    public static final int SISMEMBER = CMD_SET_BASE + 7;
    public static final int SMEMBERS = CMD_SET_BASE + 8;
    public static final int SINTERCARD = CMD_SET_BASE + 9;
    public static final int SMISMEMBER = CMD_SET_BASE + 10;
    public static final int SMOVE = CMD_SET_BASE + 11;
    public static final int SPOP = CMD_SET_BASE + 12;
    public static final int SRANDMEMBER = CMD_SET_BASE + 13;
    public static final int SREM = CMD_SET_BASE + 14;
    public static final int SUNION = CMD_SET_BASE + 15;
    public static final int SUNIONSTORE = CMD_SET_BASE + 16;
    // zset
    public static final int ZADD = CMD_ZSET_BASE + 1;
    public static final int ZCARD = CMD_ZSET_BASE + 2;
    public static final int ZCOUNT = CMD_ZSET_BASE + 3;
    public static final int ZDIFF = CMD_ZSET_BASE + 4;
    public static final int ZDIFFSTORE = CMD_ZSET_BASE + 5;
    public static final int ZINCRBY = CMD_ZSET_BASE + 6;
    public static final int ZINTER = CMD_ZSET_BASE + 7;
    public static final int ZINTERCARD = CMD_ZSET_BASE + 8;
    public static final int ZINTERSTORE = CMD_ZSET_BASE + 9;
    public static final int ZLEXCOUNT = CMD_ZSET_BASE + 10;
    public static final int ZMPOP = CMD_ZSET_BASE + 11;
    public static final int ZMSCORE = CMD_ZSET_BASE + 12;
    public static final int ZPOPMAX = CMD_ZSET_BASE + 13;
    public static final int ZPOPMIN = CMD_ZSET_BASE + 14;
    public static final int ZRANDMEMBER = CMD_ZSET_BASE + 15;
    public static final int ZRANGE = CMD_ZSET_BASE + 16;
    public static final int ZRANGESTORE = CMD_ZSET_BASE + 17;
    public static final int ZRANK = CMD_ZSET_BASE + 18;
    public static final int ZREM = CMD_ZSET_BASE + 19;
    public static final int ZREMRANGEBYLEX = CMD_ZSET_BASE + 20;
    public static final int ZREMRANGEBYRANK = CMD_ZSET_BASE + 21;
    public static final int ZREMRANGEBYSCORE = CMD_ZSET_BASE + 22;
    public static final int ZREVRANGE = CMD_ZSET_BASE + 23;
    public static final int ZREVRANGEBYLEX = CMD_ZSET_BASE + 24;
    public static final int ZREVRANGEBYSCORE = CMD_ZSET_BASE + 25;
    public static final int ZREVRANK = CMD_ZSET_BASE + 26;
    public static final int ZSCORE = CMD_ZSET_BASE + 27;
    public static final int ZUNION = CMD_ZSET_BASE + 28;
    public static final int ZUNIONSTORE = CMD_ZSET_BASE + 29;
    // script
    public static final int SCRIPT = CMD_SCRIPT_BASE + 1;
    public static final int EVAL = CMD_SCRIPT_BASE + 2;
    public static final int EVALSHA = CMD_SCRIPT_BASE + 3;

    protected static final String[] CMD_TEMPLATE_STR_SCRIPT = {
            "$6\r\nSCRIPT\r\n",   // SCRIPT
            "$4\r\nEVAL\r\n",  // EVAL
            "$7\r\nEVALSHA\r\n",  // EVALSHA
    };

    protected static final String[] CMD_TEMPLATE_STR_COMMON = {
            "$4\r\nAUTH\r\n",  // AUTH
            "$3\r\nGET\r\n",   // GET
            "$3\r\nSET\r\n",   // SET
            "$3\r\nDEL\r\n",   // DEL
            "$4\r\nPING\r\n",   // PING
            "$4\r\nDECR\r\n",   // DECR
            "$6\r\nDECRBY\r\n",   // DECRBY
            "$4\r\nECHO\r\n",   // ECHO
            "$6\r\nEXISTS\r\n",   // EXISTS
            "$6\r\nEXPIRE\r\n",   // EXPIRE
            "$8\r\nEXPIREAT\r\n",   // EXPIREAT
            "$10\r\nEXPIRETIME\r\n",   // EXPIRETIME  since 7.0.0
            "$6\r\nGETDEL\r\n",   // GETDEL   since 6.2.0
            "$8\r\nGETRANGE\r\n",   // GETRANGE
            "$6\r\nGETSET\r\n",   // GETSET
            "$4\r\nINCR\r\n",   // INCR
            "$6\r\nINCRBY\r\n",   // INCRBY
            "$11\r\nINCRBYFLOAT\r\n",   // INCRBYFLOAT
            "$4\r\nINFO\r\n",   // INFO
            "$4\r\nMGET\r\n",   // MGET
            "$4\r\nMSET\r\n",   // MSET
            "$6\r\nMSETNX\r\n",   // MSETNX
            "$9\r\nRANDOMKEY\r\n",   // RANDOMKEY
            "$6\r\nRENAME\r\n",   // RENAME
            "$8\r\nRENAMENX\r\n",   // RENAMENX
            "$6\r\nSTRLEN\r\n",   // STRLEN
            "$4\r\nTIME\r\n",   // TIME
            "$5\r\nTOUCH\r\n",   // TOUCH
            "$3\r\nTTL\r\n",   // TTL
            "$4\r\nTYPE\r\n",   // TYPE
            "$6\r\nUNLINK\r\n",   // UNLINK
            "$6\r\nAPPEND\r\n",   // APPEND
            "$5\r\nGETEX\r\n",   // GETEX
            "$8\r\nSETRANGE\r\n",   // SETRANGE
            "$6\r\nSELECT\r\n",   // SELECT
            //
            "$9\r\nSUBSCRIBE\r\n",   // SUBSCRIBE
            "$11\r\nUNSUBSCRIBE\r\n",   // UNSUBSCRIBE
            "$10\r\nPSUBSCRIBE\r\n",   // PSUBSCRIBE
            "$7\r\nPUBLISH\r\n",   // PUBLISH

    };

    protected static final String[] CMD_TEMPLATE_STR_ZSET = {
            "$4\r\nZADD\r\n",  // ZADD
            "$5\r\nZCARD\r\n",  // ZCARD
            "$6\r\nZCOUNT\r\n",  // ZCOUNT
            "$5\r\nZDIFF\r\n",  // ZDIFF
            "$10\r\nZDIFFSTORE\r\n",  // ZDIFFSTORE
            "$7\r\nZINCRBY\r\n",  // ZINCRBY
            "$6\r\nZINTER\r\n",  // ZINTER
            "$10\r\nZINTERCARD\r\n",  // ZINTERCARD
            "$11\r\nZINTERSTORE\r\n",  // ZINTERSTORE
            "$9\r\nZLEXCOUNT\r\n",  // ZLEXCOUNT
            "$5\r\nZMPOP\r\n",  // ZMPOP
            "$7\r\nZMSCORE\r\n",  // ZMSCORE
            "$7\r\nZPOPMAX\r\n",  // ZPOPMAX
            "$7\r\nZPOPMIN\r\n",  // ZPOPMIN
            "$11\r\nZRANDMEMBER\r\n",  // ZRANDMEMBER
            "$6\r\nZRANGE\r\n",  // ZRANGE
            "$11\r\nZRANGESTORE\r\n",  // ZRANGESTORE
            "$5\r\nZRANK\r\n",  // ZRANK
            "$4\r\nZREM\r\n",  // ZREM
            "$14\r\nZREMRANGEBYLEX\r\n",  // ZREMRANGEBYLEX
            "$15\r\nZREMRANGEBYRANK\r\n",  // ZREMRANGEBYRANK
            "$16\r\nZREMRANGEBYSCORE\r\n",  // ZREMRANGEBYSCORE
            "$9\r\nZREVRANGE\r\n",  // ZREVRANGE
            "$14\r\nZREVRANGEBYLEX\r\n",  // ZREVRANGEBYLEX
            "$16\r\nZREVRANGEBYSCORE\r\n",  // ZREVRANGEBYLEX
            "$8\r\nZREVRANK\r\n",  // ZREVRANK
            "$6\r\nZSCORE\r\n",  // ZSCORE
            "$6\r\nZUNION\r\n",  // ZUNION
            "$11\r\nZUNIONSTORE\r\n",  // ZUNIONSTORE

    };

    protected static final String[] CMD_TEMPLATE_STR_SET = {
            "$4\r\nSADD\r\n",   // SADD
            "$5\r\nSCARD\r\n",   // SCARD
            "$5\r\nSDIFF\r\n",   // SDIFF
            "$10\r\nSDIFFSTORE\r\n",   // SDIFFSTORE
            "$6\r\nSINTER\r\n",   // SINTER
            "$11\r\nSINTERSTORE\r\n",   // SINTERSTORE
            "$9\r\nSISMEMBER\r\n",   // SISMEMBER
            "$8\r\nSMEMBERS\r\n",   // SMEMBERS
            "$10\r\nSINTERCARD\r\n",   // SINTERCARD
            "$10\r\nSMISMEMBER\r\n",   // SMISMEMBER
            "$5\r\nSMOVE\r\n",   // SMOVE
            "$4\r\nSPOP\r\n",   // SPOP
            "$11\r\nSRANDMEMBER\r\n",   // SRANDMEMBER
            "$4\r\nSREM\r\n",   // SREM
            "$6\r\nSUNION\r\n",   // SUNION
            "$11\r\nSUNIONSTORE\r\n",   // SUNIONSTORE

    };

    protected static final String[] CMD_TEMPLATE_STR_LIST = {
            "$6\r\nLINDEX\r\n",   // LINDEX
            "$7\r\nLINSERT\r\n",   // LINSERT
            "$4\r\nLLEN\r\n",   // LLEN
            "$5\r\nLMOVE\r\n",   // LMOVE
            "$4\r\nLPOP\r\n",   // LPOP
            "$5\r\nLPUSH\r\n",   // LPUSH
            "$6\r\nLPUSHX\r\n",   // LPUSHX
            "$6\r\nLRANGE\r\n",   // LRANGE
            "$4\r\nLREM\r\n",   // LREM
            "$4\r\nLSET\r\n",   // LSET
            "$5\r\nLTRIM\r\n",   // LTRIM
            "$4\r\nRPOP\r\n",   // RPOP
            "$9\r\nRPOPLPUSH\r\n",   // RPOPLPUSH
            "$5\r\nRPUSH\r\n",   // RPUSH
            "$6\r\nRPUSHX\r\n",   // RPUSHX
    };

    protected static final String[] CMD_TEMPLATE_STR_HASH = {
            "$4\r\nHDEL\r\n",   // HDEL
            "$7\r\nHEXISTS\r\n",   // HEXISTS
            "$4\r\nHGET\r\n",   // HGET
            "$7\r\nHGETALL\r\n",   // HGETALL
            "$7\r\nHINCRBY\r\n",   // HINCRBY
            "$12\r\nHINCRBYFLOAT\r\n",   // HINCRBYFLOAT
            "$5\r\nHKEYS\r\n",   // HKEYS
            "$4\r\nHLEN\r\n",   // HLEN
            "$5\r\nHMGET\r\n",   // HMGET
            "$5\r\nHMSET\r\n",   // HMSET
            "$10\r\nHRANDFIELD\r\n",   // HRANDFIELD
            "$4\r\nHSET\r\n",   // HSET
            "$6\r\nHSETNX\r\n",   // HSETNX
            "$7\r\nHSTRLEN\r\n",   // HSTRLEN
            "$5\r\nHVALS\r\n",   // HVALS
    };

    protected static byte[] CMD_LEN_TEMPLATE;
    //            = {
//            4, '*','1','\r','\n',   0,0,0,0,0,
//            4, '*','2','\r','\n',   0,0,0,0,0,
//            4, '*','3','\r','\n',   0,0,0,0,0,
//            4, '*','4','\r','\n',   0,0,0,0,0,
//            4, '*','5','\r','\n',   0,0,0,0,0,
//            4, '*','6','\r','\n',   0,0,0,0,0,
//            4, '*','7','\r','\n',   0,0,0,0,0,
//            4, '*','8','\r','\n',   0,0,0,0,0,
//            4, '*','9','\r','\n',   0,0,0,0,0,
//            5, '*','1','0','\r','\n',   0,0,0,0,
//
//    };
    protected static final int CMD_LEN_TEMPLATE_MAX = 120; //CMD_LEN_TEMPLATE.length / 10;

    protected static byte[] CMD_TEMPLATE;
    private static final int CMD_TEMPLATE_LINE = 30;
    static {
        int cmdCommonNum = CMD_TEMPLATE_STR_COMMON.length;
        int cmdHashNum = CMD_TEMPLATE_STR_HASH.length;
        int cmdListNum = CMD_TEMPLATE_STR_LIST.length;
        int cmdSetNum = CMD_TEMPLATE_STR_SET.length;
        int cmdZSetNum = CMD_TEMPLATE_STR_ZSET.length;
        int cmdScriptNum = CMD_TEMPLATE_STR_SCRIPT.length;
        try {
            int bytesLen = (cmdCommonNum + cmdHashNum + cmdListNum + cmdSetNum + cmdZSetNum + cmdScriptNum) * CMD_TEMPLATE_LINE;
            CMD_TEMPLATE = new byte[bytesLen];
            int idxWrite = 0;
            for (String str : CMD_TEMPLATE_STR_COMMON) {
                byte[] tmpBytes = str.getBytes(CharsetUtil.UTF_8);
                CMD_TEMPLATE[idxWrite++] = (byte) tmpBytes.length;
                System.arraycopy(tmpBytes, 0, CMD_TEMPLATE, idxWrite, tmpBytes.length);
                idxWrite += CMD_TEMPLATE_LINE - 1;
            }
            for (String str : CMD_TEMPLATE_STR_HASH) {
                byte[] tmpBytes = str.getBytes(CharsetUtil.UTF_8);
                CMD_TEMPLATE[idxWrite++] = (byte) tmpBytes.length;
                System.arraycopy(tmpBytes, 0, CMD_TEMPLATE, idxWrite, tmpBytes.length);
                idxWrite += CMD_TEMPLATE_LINE - 1;
            }
            for (String str : CMD_TEMPLATE_STR_LIST){
                byte[] tmpBytes = str.getBytes(CharsetUtil.UTF_8);
                CMD_TEMPLATE[idxWrite++] = (byte) tmpBytes.length;
                System.arraycopy(tmpBytes, 0, CMD_TEMPLATE, idxWrite, tmpBytes.length);
                idxWrite += CMD_TEMPLATE_LINE - 1;
            }
            for (String str : CMD_TEMPLATE_STR_SET){
                byte[] tmpBytes = str.getBytes(CharsetUtil.UTF_8);
                CMD_TEMPLATE[idxWrite++] = (byte) tmpBytes.length;
                System.arraycopy(tmpBytes, 0, CMD_TEMPLATE, idxWrite, tmpBytes.length);
                idxWrite += CMD_TEMPLATE_LINE - 1;
            }
            for (String str : CMD_TEMPLATE_STR_ZSET){
                byte[] tmpBytes = str.getBytes(CharsetUtil.UTF_8);
                CMD_TEMPLATE[idxWrite++] = (byte) tmpBytes.length;
                System.arraycopy(tmpBytes, 0, CMD_TEMPLATE, idxWrite, tmpBytes.length);
                idxWrite += CMD_TEMPLATE_LINE - 1;
            }
            for (String str : CMD_TEMPLATE_STR_SCRIPT){
                byte[] tmpBytes = str.getBytes(CharsetUtil.UTF_8);
                CMD_TEMPLATE[idxWrite++] = (byte) tmpBytes.length;
                System.arraycopy(tmpBytes, 0, CMD_TEMPLATE, idxWrite, tmpBytes.length);
                idxWrite += CMD_TEMPLATE_LINE - 1;
            }
            //
            int idx;
            CMD_LEN_TEMPLATE = new byte[CMD_LEN_TEMPLATE_MAX*10];
            for(int i=0; i<CMD_LEN_TEMPLATE_MAX; ++i){
                idx = i*10;
                int num = i + 1;
                if(num < 10){   // x
                    CMD_LEN_TEMPLATE[idx++] = 4;
                    CMD_LEN_TEMPLATE[idx++] = '*';
                    CMD_LEN_TEMPLATE[idx++] = (byte) (num + 48);
                }else if(num < 100){ // xx
                    CMD_LEN_TEMPLATE[idx++] = 5;
                    CMD_LEN_TEMPLATE[idx++] = '*';
                    CMD_LEN_TEMPLATE[idx++] = (byte) (num/10 + 48);
                    CMD_LEN_TEMPLATE[idx++] = (byte) ((num%10) + 48);
                }else if(num < 1000){  // xxx
                    CMD_LEN_TEMPLATE[idx++] = 6;
                    CMD_LEN_TEMPLATE[idx++] = '*';
                    CMD_LEN_TEMPLATE[idx++] = (byte) (num/100 + 48);
                    CMD_LEN_TEMPLATE[idx++] = (byte) ((num%100)/10 + 48);
                    CMD_LEN_TEMPLATE[idx++] = (byte) ((num%10) + 48);
                }
                CMD_LEN_TEMPLATE[idx++] = '\r';
                CMD_LEN_TEMPLATE[idx] = '\n';
            }
        }catch (Throwable e){
            e.printStackTrace();
        }

    }


}
