package fun.lib.ejnode.core.db.redis;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public final class RedisDecoder {

    private int _stat;
    private Stack<ArrayWrap> _statStack;
    private ByteBuf _bufCache;
    private final int _cacheMax;
    private int _cacheUsed;
    private LinkedList<Object> _lsRet;
    private int _retNum;

    private int _skipBytes;
    private long _curInt;
    private boolean _curIntNeg;
    private String _curStr;
    private int _bulkReadCnt;
    private int _curBulkSize;

    private boolean _onReadSize;

    public RedisDecoder(ByteBuf bufCache, int cacheMax){
        _bufCache = bufCache;
        _cacheMax = cacheMax;
        _cacheUsed = 0;
        _stat = STAT_IDLE;
        _statStack = new Stack<>();
        _lsRet = new LinkedList<>();
        _skipBytes = 0;
        _retNum = 0;
    }

    public void onRecv(ByteBuf buf) throws Exception {
        // debug
//        String s = (String) buf.getCharSequence(buf.readerIndex(), buf.readableBytes(), CharsetUtil.UTF_8);
//        int n = 1;
        //
        while (buf.readableBytes() > 0){
            if(_skipBytes > 0){
                int skip = Math.min(_skipBytes, buf.readableBytes());
                buf.readBytes(skip);
                _skipBytes -= skip;
                continue;
            }
            if(_stat == STAT_IDLE){
                _stat = _parseType(buf);
                _initReadInt();
                _onReadSize = true;
            }
            else if(_stat == STAT_READ_INT){
                if(_doReadInt(buf)){
                    _skipBytes = 1;
                    _procItemRet(_curInt);
                    _stat = STAT_IDLE;
                }
            }
            else if(_stat == STAT_READ_BULK){
                if(_onReadSize){   // on reading bulkSize
                    if(_doReadInt(buf)){
                        _onReadSize = false;
                        _curBulkSize = (int) _curInt;
                        if(_curBulkSize > 0){
                            _skipBytes = 1;
                            _bulkReadCnt = 0;
                        }else if(_curBulkSize == 0){    // empty string
                            _skipBytes = 3;
                            _procItemRet("");
                            _stat = STAT_IDLE;
                        }else {   // null
                            _skipBytes = 1;
                            _procItemRet(null);
                            _stat = STAT_IDLE;
                        }
                    }
                }else {
                    int needRead = _curBulkSize - _bulkReadCnt;
                    int srcBytes = buf.readableBytes();
                    if(srcBytes >= needRead){   // bulk end
                        String str;
                        if(_bufCache.readableBytes() > 0){
                            _checkCacheWritable(needRead);
                            _bufCache.writeBytes(buf, needRead);
                            // proc bufCache
                            str = (String) _bufCache.readCharSequence(_bufCache.readableBytes(), CharsetUtil.UTF_8);
                            _cacheUsed = 0;
                        }else {
                            str = (String) buf.readCharSequence(needRead, CharsetUtil.UTF_8);
                        }
                        _procItemRet(str);
                        _skipBytes = 2;
                        _stat = STAT_IDLE;
                    }else {
                        _checkCacheWritable(srcBytes);
                        _bufCache.writeBytes(buf);
                        _cacheUsed += srcBytes;
                        _bulkReadCnt += srcBytes;
                    }
                }
            }
            else if(_stat == STAT_READ_ARR){
                if(_onReadSize){
                    if(_doReadInt(buf)){   //read size done
                        _onReadSize = false;
                        int lsSize = (int) _curInt;
                        if(lsSize > 0){
                            ArrayWrap arr = new ArrayWrap(lsSize);
                            _statStack.push(arr);
                        }else if(lsSize == 0){   // empty list
                            _procItemRet(new ArrayList<>());
                        }else{   // null, not exist
                            _procItemRet(null);
                        }
                        _skipBytes = 1;
                        _stat = STAT_IDLE;
                    }
                }
            }
            else if(_stat == STAT_READ_STR){
                if(_doReadStr(buf)){
                    _skipBytes = 2;
                    _procItemRet(_curStr);
                    _stat = STAT_IDLE;
                }
            }
            else if(_stat == STAT_READ_ERR){
                if(_doReadStr(buf)){
                    _skipBytes = 2;
                    _procItemRet(new Exception(_curStr));
                    _stat = STAT_IDLE;
                }
            }
        }
    }

    private void _initReadInt(){
        _curInt = 0;
        _curIntNeg = false;
    }
    private boolean _doReadInt(ByteBuf buf){
        byte b;
        while (buf.readableBytes() > 0){
            b = buf.readByte();
            if(b == '-'){   //negative
                _curIntNeg = true;
            }else if(b == '\r'){
                if(_curIntNeg){
                    _curInt = -_curInt;
                }
                return true;
            }else {
                _curInt = _curInt * 10 + b - '0';
            }
        }
        return false;
    }

    private void _checkCacheWritable(int willWriteSize) throws Exception {
        if(_cacheUsed + willWriteSize > _cacheMax){
            throw new Exception("RedisDecoder cache buffer overflow, used="+_cacheUsed+", add="+willWriteSize+", maxLimit="+_cacheMax);
        }
//        if(_bufCache.writableBytes() < willWriteSize){
//            throw new Exception("cache buffer overflow, maxLimit="+_bufCache.capacity());
//        }
    }

    private boolean _doReadStr(ByteBuf buf) throws Exception {
        boolean endFound = false;
        buf.markReaderIndex();
        while (buf.readableBytes() > 0){
            if(buf.readByte() == '\r'){
                endFound = true;
                break;
            }
        }
        int idxEnd = buf.readerIndex();
        buf.resetReaderIndex();
        if(endFound){
            int readSize = idxEnd - 1 - buf.readerIndex();
            if(_bufCache.readableBytes() > 0){  // has cache data, merge
                if(readSize > 0){
                    _checkCacheWritable(readSize);
                    _bufCache.writeBytes(buf, readSize);
                }
                _curStr = (String) _bufCache.readCharSequence(_bufCache.readableBytes(), CharsetUtil.UTF_8);
                _cacheUsed = 0;
            }else{
                if(readSize > 0){
                    _curStr = (String) buf.readCharSequence(readSize, CharsetUtil.UTF_8);
                }else {
                    _curStr = "";
                }
            }
            return true;
        }else {   // end char not found, copy to cache
            int srcBytes = buf.readableBytes();
            _checkCacheWritable(srcBytes);
            _bufCache.writeBytes(buf);
            _cacheUsed += srcBytes;
        }
        return false;
    }

    private void _procItemRet(Object ret){
        if(_statStack.isEmpty()){
            _lsRet.offer(ret);
            ++_retNum;
//            return true;
            return;
        }
        ArrayWrap arr = _statStack.peek();
        if(arr.addObj(ret) == arr.size){
            _lsRet.offer(arr.ls);
            ++_retNum;
            _statStack.pop();
//            return true;
        }
//        return false;
    }

    public Object popResult(){
        --_retNum;
        return _lsRet.poll();
    }
    public int resultNum(){
        return _retNum;
    }
    public void onRelease(){
        if(_bufCache != null){
            _bufCache.release();
            _bufCache = null;
        }
    }

    private int _parseType(ByteBuf buf) throws Exception {
        byte type = buf.readByte();
        if(type == '+'){  // simple string
            return TYPE_STR;
        }
        if(type == '$'){  // bulk
            return TYPE_BULK;
        }
        if(type == '*'){   //array
            return TYPE_ARR;
        }
        if(type == ':'){ // int
            return TYPE_INT;
        }
        if(type == '-'){  // err
            return TYPE_ERR;
        }
        throw new Exception(String.format("unknown data type: %c", type));
    }

    private static final int STAT_IDLE = 1;
    private static final int STAT_READ_STR = 2;
    private static final int STAT_READ_ERR = 3;
    private static final int STAT_READ_INT = 4;
    private static final int STAT_READ_BULK = 5;
    private static final int STAT_READ_ARR = 6;
    //
    private static final int TYPE_STR = 2;
    private static final int TYPE_ERR = 3;
    private static final int TYPE_INT = 4;
    private static final int TYPE_BULK = 5;
    private static final int TYPE_ARR = 6;

    static class ArrayWrap{
        public final int size;
        public List<Object> ls;

        private ArrayWrap(int size){
            this.size = size;
        }

        public int addObj(Object obj){
            if(ls == null){
                ls = new ArrayList<>(size);
            }
            ls.add(obj);
            return ls.size();
        }
    }

}


