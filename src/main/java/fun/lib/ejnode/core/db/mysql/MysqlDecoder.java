package fun.lib.ejnode.core.db.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public final class MysqlDecoder {

    private int _stat;
    private int _phase;

    private int _packLen;
    private int _packSeqId;
    private int _payloadCnt;
    private long _capabilityFlags;
    private byte[] _authPlug;
    private String _authPlugName;

    public MysqlDecoder(){
        _stat = STAT_PACK_HEAD;
        _phase = PHASE_CONN;

        _packLen = -1;
        _packSeqId = -1;
        _capabilityFlags = 0;
    }

    public void onRecv(ByteBuf buf) throws Exception{
        while (buf.readableBytes() > 0){
            if(_stat == STAT_PACK_HEAD){
                _readPackHead(buf);
            }else if(_stat == STAT_PAYLOAD){
                int lenBegin = buf.readableBytes();
                if(_phase == PHASE_CMD){

                }else{  // phase conn
                    _readHandshake(buf);
                }
                int hasRead = lenBegin - buf.readableBytes();
                _payloadCnt -= hasRead;
                if(_payloadCnt == 0){
                    _stat = STAT_PACK_HEAD;
                }
            }
        }
    }
    private void _readPackHead(ByteBuf buf){
        _packLen = buf.readMediumLE();
        _packSeqId = buf.readUnsignedByte();
        _payloadCnt = _packLen;
        _stat = STAT_PAYLOAD;
    }
    private void _readHandshake(ByteBuf buf){
        //https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html
        int protoVer = buf.readUnsignedByte();
        String strVer = _readStrNullTerm(buf);
        int connId = buf.readIntLE();
        byte[] authPlug = new byte[256];
        buf.readBytes(authPlug, 0, 8);
        buf.skipBytes(1);    // termOf  authPlugDataPart1
        long capabilityLow = buf.readUnsignedShortLE();
        int protoCharset = buf.readUnsignedByte();
        int svrStatusFlag = buf.readUnsignedShortLE();
        _capabilityFlags = buf.readUnsignedShortLE();
        _capabilityFlags = (_capabilityFlags<<16)|capabilityLow;
        int authPlugDataLen = 0;
//        long ret = _capabilityFlags & CapabilityFlags.CLIENT_PLUGIN_AUTH;
        if( (_capabilityFlags & CapabilityFlags.CLIENT_PLUGIN_AUTH) != 0 ){
            authPlugDataLen = buf.readUnsignedByte();
        }else{
            buf.skipBytes(1);
        }
        buf.skipBytes(10); //reserved. All 0s.
        int authPlugPart2Len = Math.max(13, authPlugDataLen - 8);
        buf.readBytes(authPlug, 8, authPlugPart2Len);
        if( (_capabilityFlags & CapabilityFlags.CLIENT_PLUGIN_AUTH) != 0){
            _authPlugName = _readStrNullTerm(buf);
        }
    }

    private String _readStrNullTerm(ByteBuf buf){
        int idxBegin = buf.readerIndex();
        int idxEnd = idxBegin + buf.readableBytes();
        int idx = idxBegin;
        while (idx < idxEnd){
            if(buf.getByte(idx++) == 0){
                String str = (String) buf.readCharSequence(idx - idxBegin - 1, CharsetUtil.UTF_8);
                buf.skipBytes(1);
                return str;
            }
        }
        return null;
    }
    private long _readIntLenEnc(ByteBuf buf){
        int b1 = buf.readUnsignedByte();
        if(b1 < 251){
            return b1;
        }
        if(b1 == 0xFC){
            return buf.readUnsignedShortLE();
        }
        if(b1 == 0xFD){
            return buf.readMediumLE();
        }
        if(b1 == 0xFE){
            return buf.readLongLE();
        }
        return -1;
    }

    private static final int STAT_PACK_HEAD = 0;
    private static final int STAT_PAYLOAD = 1;
    //
    private static final int PHASE_CONN = 10;
    private static final int PHASE_CMD = 11;

    static class CapabilityFlags{
        static final long CLIENT_PROTOCOL_41 = 512;
        static final long CLIENT_PLUGIN_AUTH = 1L<<19;
    }
}
