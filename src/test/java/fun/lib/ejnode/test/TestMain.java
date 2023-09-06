package fun.lib.ejnode.test;

import fun.lib.ejnode.api.*;
import fun.lib.ejnode.api.Process;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.api.net.*;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.LoopWorker;
import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.db.mysql.MysqlClient;
import fun.lib.ejnode.core.db.redis.*;
import fun.lib.ejnode.core.net.ClientCodec;
import fun.lib.ejnode.core.net.ServerCodec;
import fun.lib.ejnode.core.pool.Pool;
import fun.lib.ejnode.util.timer.DFTimeout;
import fun.lib.ejnode.util.timer_bak.DFHiWheelTimerBak;
import fun.lib.ejnode.util.timer_bak.DFTimeoutBak;
import fun.lib.ejnode.util.timer.DFHiWheelTimer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestMain {

    public static void main(String[] args){
        System.out.println("aaaa");

        System.out.println("thlWorkerId 1: "+LoopWorker.getThlWorker());

//        DFIdxVerList<Integer> lsInt = new DFIdxVerList<>();
//        long id1 = lsInt.add(1);
//        long id2 = lsInt.add(2);
//        long id3 = lsInt.add(3);
//        long id4 = lsInt.add(4);
//        long id5 = lsInt.add(5);
//        Integer int1 = lsInt.get(id1);
//        Integer int2 = lsInt.get(id2);
//        Integer int3 = lsInt.get(id3);
//        Integer int4 = lsInt.get(id4);
//        Integer int5 = lsInt.get(id5);
//
//        int3 = lsInt.remove(id3);
//        long id6 = lsInt.add(3);
//        int3 = lsInt.get(id3);
//        int3 = lsInt.get(id6);


//        int len = 7;
//        for(int i=0; i<20; ++i){
//            System.out.print((i&len)+", ");
//        }
//        System.out.println();

//        DFObjPool<String> objPool = new DFObjPool<>(100);
//        String str1 = objPool.newObj();
//        objPool.recycle(str1);

        EJNode node = EJNode.get();
//        node.entry(redisPoolTest1.class)
        node.entry(HttpServerTest1.class)
                .logLevel(Logger.LEVEL_DEBUG)
//                .forkParams(node.forkParamsBuilder().ioServerGroupThreadNum(4).build())
                .start();

//        redisDecodeTest1();

//        regexTest1();

//        ConcurrentLinkedQueue<String> que = new ConcurrentLinkedQueue<String>();
//        que.poll();

//        AtomicInteger num = new AtomicInteger(0);
//        num.get();

//        timerTest1();

//        timerTest2();

        //waitAccuracyTest();

        //cacheLineTest();
    }

    static class mysqlPoolTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Db db = ctx.db;
            Logger log = ctx.logger;
            Timer timer = ctx.timer;
            //
            log.info("mysqlPoolTest1 onStart()");
            String host = "127.0.0.1";
            int port = 3306;
            //
//            Pool<MysqlClient> pool = db.mysql().createPool()
//                    .connConfig(host, port, "")
//                    .connTimeout(5000)
//                    .poolSize(1)
//                    .start();
//            pool.book((error, client) -> {
//
//            });
        }
    }

    static class redisPoolTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Db db = ctx.db;
            Logger log = ctx.logger;
            Timer timer = ctx.timer;
            //
            log.info("redisPoolTest1 onStart()");
            String host = "8.210.10.221";
            int port = 11000;
            String pwd = "L38(3#j,27qWqE@";
            final Pool<RedisClient> pool = db.redis().createPool()
                    .connConfig(host, port, pwd)
                    .connTimeout(5000)
                    .poolSize(1)
                    .start();
                    pool.book((error, client) -> {
                        log.info("bookRet: error="+error);
                        if(error != null){
                            return;
                        }
//                        // script test
//                        try {
//                            client.apiScript().scriptLoad("return 123").onResult((error1, result) -> {
//                                log.info("scriptLoad ret: error="+error1+", ret="+result);
//                            });
//                            client.apiScript().eval("return2 111", "0").onResult((error1, result) -> {
//                                log.info("eval ret: error="+error1+", ret="+result);
//                            });
//                            client.apiScript().evalSHA("30dc9ef2a8d563dced9a243ecd0cc449c2ea0144", "0").onResult((error1, result) -> {
//                                log.info("evalSHA ret: error="+error1+", ret="+result);
//                            });
//                            client.apiScript().scriptExists("111","","30dc9ef2a8d563dced9a243ecd0cc449c2ea0144").onResult((error1, result) -> {
//                                log.info("scriptExists ret: error="+error1+", ret="+result);
//                            });
//                            client.apiScript().scriptKill().onResult((error1, result) -> {
//                                log.info("scriptKill ret: error="+error1+", ret="+result);
//                            });
//                        }finally {
//                            client.release();
//                        }
//                        if(log != null){
//                            return;
//                        }

//                        // subs test
//                        try {
////                            client.unsubscribe();
//                            client
////                                    .unsubscribe("ch111", "ch111")
//                                    .unsubscribe()
//                                    .onUnsubsResult((error1, channel, subsNum) -> {
//                                log.info("unsubscribe: error="+error1+", channel="+channel+", subsNum="+subsNum);
//                            });
//                            timer.timeout(6000, ()->{
//                                client.get("test1").onResult((error1, result) -> {
//
//                                });
//                                client.release();
//                            });
//                        }finally {
//
//                        }
//                        if(log != null){
//                            return;
//                        }

                        try {
                            client.ping().onResult((error1, result) -> {
                                log.info("recv pingRet: error="+error1+", ret="+result);
                            });
                            client.decr("test123321").onResult((error1, result) -> {
                                log.info("decr ret: "+error1+", "+result);
                            });
                            client.decrBy("test123321", 2).onResult((error1, result) -> {
                                log.info("decrBy ret: "+error1+", "+result);
                            });
                            client.incr("test123321").onResult((error1, result) -> {
                                log.info("incr ret: "+error1+", "+result);
                            });
                            client.incrBy("test123321", 11).onResult((error1, result) -> {
                                log.info("incrBy ret: "+error1+", "+result);
                            });
                            client.incrByFloat("test123321", 3.1415926f).onResult((error1, result) -> {
                                log.info("incrByFloat ret: "+error1+", "+result);
                            });
                            client.echo("test123321").onResult((error1, result) -> {
                                log.info("echo ret: "+error1+", "+result);
                            });
                            client.exists("test123321","aaa").onResult((error1, result) -> {
                                log.info("exists ret: "+error1+", "+result);
                            });
                            client.expire("test123321",10).onResult((error1, result) -> {
                                log.info("expire ret: "+error1+", "+result);
                            });
                            client.expireAt("test123321",1234567).onResult((error1, result) -> {
                                log.info("expireAt ret: "+error1+", "+result);
                            });
                            client.expireTime("test123321").onResult((error1, result) -> {
                                log.info("expireTime ret: "+error1+", "+result);
                            });
                            client.getDel("test123321").onResult((error1, result) -> {
                                log.info("getDel ret: "+error1+", "+result);
                            });
                            client.set("test123321", "aaa").onResult((error1, result) -> {
                                log.info("set ret: "+error1+", "+result);
                            });
                            client.getRange("test123321", 0, 10).onResult((error1, result) -> {
                                log.info("getRange ret: "+error1+", "+result);
                            });
                            client.getSet("test123321", "aaa").onResult((error1, result) -> {
                                log.info("getSet ret: "+error1+", "+result);
                            });
                            client.mSet("test123321","111", "aaa","222").onResult((error1, result) -> {
                                log.info("mset ret: "+error1+", "+result);
                            });
                            client.mGet("test123321", "aaa").onResult((error1, result) -> {
                                log.info("mget ret: "+error1+", "+result);
                            });
                            client.mSetNX("test123321","111", "aaa","222").onResult((error1, result) -> {
                                log.info("msetnx ret: "+error1+", "+result);
                            });
                            client.randomKey().onResult((error1, result) -> {
                                log.info("randomKey ret: "+error1+", "+result);
                            });
                            client.randomKey().onResult((error1, result) -> {
                                log.info("randomKey ret: "+error1+", "+result);
                            });
                            client.rename("test123321", "test1233212").onResult((error1, result) -> {
                                log.info("rename ret: "+error1+", "+result);
                            });
                            client.renameNX("test123321", "test1233212").onResult((error1, result) -> {
                                log.info("renameNX ret: "+error1+", "+result);
                            });
                            client.del("test123321");
                            client.del("test1233212");

                            RedisApiHash hash = client.apiHash();
                            hash.set("test123321", "aaa", "111").onResult((error1, result) -> {
                                log.info("hSet ret: "+error1+", "+result);
                            });
                            hash.set("test123321", "aaa", "111","bbb","222","ccc","333","ddd","444","eee","555").onResult((error1, result) -> {
                                log.info("hSet multi ret: "+error1+", "+result);
                            });
                            hash.del("test123321", "aaa", "bbb").onResult((error1, result) -> {
                                log.info("hdel ret: "+error1+", "+result);
                            });
                            hash.exists("test123321", "aaa").onResult((error1, result) -> {
                                log.info("hexists ret: "+error1+", "+result);
                            });
                            hash.get("test123321", "aaa").onResult((error1, result) -> {
                                log.info("hget ret: "+error1+", "+result);
                            });
                            hash.getAll("test123321").onResult((error1, result) -> {
                                log.info("hgetall ret: "+error1+", "+result);
                            });
                            hash.incrBy("test123321", "aaa", 2).onResult((error1, result) -> {
                                log.info("hincrby ret: "+error1+", "+result);
                            });
                            hash.incrByFloat("test123321", "aaa", 2.567f).onResult((error1, result) -> {
                                log.info("hincrby ret: "+error1+", "+result);
                            });
                            hash.keys("test123321").onResult((error1, result) -> {
                                log.info("hKeys ret: "+error1+", "+result);
                            });
                            hash.len("test123321").onResult((error1, result) -> {
                                log.info("hLen ret: "+error1+", "+result);
                            });
                            hash.mGet("test123321", "aaa", "bbb").onResult((error1, result) -> {
                                log.info("hMGet ret: "+error1+", "+result);
                            });
                            hash.randField("test123321").onResult((error1, result) -> {
                                log.info("hRandField ret: "+error1+", "+result);
                            });
                            hash.randField("test123321", -5, true).onResult((error1, result) -> {
                                log.info("hRandField(count) ret: "+error1+", "+result);
                            });
                            hash.setNX("test123321", "ddd", "666").onResult((error1, result) -> {
                                log.info("hSetNX ret: "+error1+", "+result);
                            });
                            hash.strLen("test123321", "ccc").onResult((error1, result) -> {
                                log.info("hStrLen ret: "+error1+", "+result);
                            });
                            hash.vals("test123321").onResult((error1, result) -> {
                                log.info("hVals ret: "+error1+", "+result);
                            });
                            //
                            client.info().onResult((error1, result) -> {
                                log.info("info ret: "+error1+", "+result);
                            });
                            client.del("test123321").onResult((error1, result) -> {
                                log.info("del ret: "+error1+", "+result);
                            });
                            RedisApiList list = client.apiList();
                            list.push("abccba123","ele0","ele1","ele2","ele3","ele4","ele5").onResult((error1, result) -> {
                                log.info("lPush ret: "+error1+", "+result);
                            });
                            list.index("abccba123", 0).onResult((error1, result) -> {
                                log.info("lIndex ret: "+error1+", "+result);
                            });
                            list.insertBefore("abccba123", "ele0", "ele1").onResult((error1, result) -> {
                                log.info("lInsertBefore ret: "+error1+", "+result);
                            });
                            list.insertAfter("abccba123", "ele0", "ele1").onResult((error1, result) -> {
                                log.info("lInsertAfter ret: "+error1+", "+result);
                            });
                            list.len("abccba123").onResult((error1, result) -> {
                                log.info("lLen ret: "+error1+", "+result);
                            });
                            list.move("abccba123", "abccba321", true, false).onResult((error1, result) -> {
                                log.info("lMove ret: "+error1+", "+result);
                            });
                            list.pop("abccba123").onResult((error1, result) -> {
                                log.info("lPop ret: "+error1+", "+result);
                            });
                            list.pop("abccba123", 1).onResult((error1, result) -> {
                                log.info("lPop(count) ret: "+error1+", "+result);
                            });
                            list.pushX("abccba123","ele0").onResult((error1, result) -> {
                                log.info("lPushX ret: "+error1+", "+result);
                            });
                            list.range("abccba123", 1, 5).onResult((error1, result) -> {
                                log.info("lRange ret: "+error1+", "+result);
                            });
                            list.rem("abccba123", 1, "ele3").onResult((error1, result) -> {
                                log.info("lRem ret: "+error1+", "+result);
                            });
                            list.set("abccba123", 3, "ele3-0").onResult((error1, result) -> {
                                log.info("lSet ret: "+error1+", "+result);
                            });
                            list.trim("abccba123", 0, 1).onResult((error1, result) -> {
                                log.info("lTrim ret: "+error1+", "+result);
                            });
                            list.rpop("abccba123").onResult((error1, result) -> {
                                log.info("rpop ret: "+error1+", "+result);
                            });
                            list.rpop("abccba123", 2).onResult((error1, result) -> {
                                log.info("rpop(count) ret: "+error1+", "+result);
                            });
                            list.rpush("abccba123", "ele-11","ele-11","ele-12","ele-13","ele-4").onResult((error1, result) -> {
                                log.info("rpush ret: "+error1+", "+result);
                            });
                            list.rpushx("abccba123", "ele-11","ele-11","ele-22","ele-13","ele-24").onResult((error1, result) -> {
                                log.info("rpushx ret: "+error1+", "+result);
                            });
                            list.rpushx("abccba123", "ele-24").onResult((error1, result) -> {
                                log.info("rpushx(single) ret: "+error1+", "+result);
                            });
                            list.rpoplpush("abccba123", "abccba123-list2").onResult((error1, result) -> {
                                log.info("rpoplpush ret: "+error1+", "+result);
                            });
                            list.brpop("abccba123-", "listTest222", "5").onResult((error1, result) -> {
                                log.info("brpop ret: "+error1+", "+result);
                            });
                            //
                            RedisApiSet set = client.apiSet();
                            set.add("setTest111", "111","222","333","444","555","666").onResult((error1, result) -> {
                                log.info("sAdd ret: "+error1+", "+result);
                            });
                            set.card("setTest111").onResult((error1, result) -> {
                                log.info("sCard ret: "+error1+", "+result);
                            });
                            set.diff("setTest111").onResult((error1, result) -> {
                                log.info("sDiff ret: "+error1+", "+result);
                            });
                            set.diffStore("setTest222", "setTest111").onResult((error1, result) -> {
                                log.info("sDiffStore ret: "+error1+", "+result);
                            });
                            set.inter("setTest222", "setTest111").onResult((error1, result) -> {
                                log.info("sInter ret: "+error1+", "+result);
                            });
                            set.interStore("setTest222", "setTest111").onResult((error1, result) -> {
                                log.info("sInterStore ret: "+error1+", "+result);
                            });
                            set.isMember("setTest111", "333").onResult((error1, result) -> {
                                log.info("sIsMember ret: "+error1+", "+result);
                            });
                            set.members("setTest111").onResult((error1, result) -> {
                                log.info("sMembers ret: "+error1+", "+result);
                            });
                            set.interCard("setTest111", "setTest222").onResult((error1, result) -> {
                                log.info("interCard ret: "+error1+", "+result);
                            });
                            set.mIsMember("setTest111", "ele0","555").onResult((error1, result) -> {
                                log.info("mIsMember ret: "+error1+", "+result);
                            });
                            set.move("setTest111", "setTest222","222").onResult((error1, result) -> {
                                log.info("move ret: "+error1+", "+result);
                            });
                            set.pop("setTest111").onResult((error1, result) -> {
                                log.info("pop ret: "+error1+", "+result);
                            });
                            set.pop("setTest111", 2).onResult((error1, result) -> {
                                log.info("pop(count) ret: "+error1+", "+result);
                            });
                            set.randMember("setTest111").onResult((error1, result) -> {
                                log.info("randMember ret: "+error1+", "+result);
                            });
                            set.randMember("setTest111", 2).onResult((error1, result) -> {
                                log.info("randMember(count) ret: "+error1+", "+result);
                            });
                            set.rem("setTest111", "666", "111").onResult((error1, result) -> {
                                log.info("rem ret: "+error1+", "+result);
                            });
                            set.union("setTest111", "setTest222").onResult((error1, result) -> {
                                log.info("union ret: "+error1+", "+result);
                            });
                            set.unionStore("setTest333", "setTest111", "setTest222").onResult((error1, result) -> {
                                log.info("unionStore ret: "+error1+", "+result);
                            });
                            client.set("asdasd222", "aaa111");
                            client.strLen("asdasd222").onResult((error1, result) -> {
                                log.info("strLen ret: "+error1+", "+result);
                            });
                            client.time().onResult((error1, result) -> {
                                log.info("time ret: "+error1+", "+result);
                            });
                            client.touch("setTest222", "asdasd222", "sdfasdff").onResult((error1, result) -> {
                                log.info("touch ret: "+error1+", "+result);
                            });
                            client.ttl("setTest222").onResult((error1, result) -> {
                                log.info("ttl ret: "+error1+", "+result);
                            });
                            client.type("setTest222").onResult((error1, result) -> {
                                log.info("type ret: "+error1+", "+result);
                            });
                            client.unlink("setTest222", "asdasd222").onResult((error1, result) -> {
                                log.info("unlink ret: "+error1+", "+result);
                            });
                            // zet
                            RedisApiZSet zset = client.apiZSet();
                            zset.add("zset111", "80","aaa", "75","bbb","90","ccc","92","ddd").onResult((error1, result) -> {
                                log.info("zadd ret: "+error1+", "+result);
                            });
                            zset.add("zset222", "60","aaa2", "70","bbb","95","ccc2","81","ddd").onResult((error1, result) -> {
                                log.info("zadd ret: "+error1+", "+result);
                            });
                            zset.card("zset111").onResult((error1, result) -> {
                                log.info("zcard ret: "+error1+", "+result);
                            });
                            zset.count("zset111", 85, 100).onResult((error1, result) -> {
                                log.info("zcount ret: "+error1+", "+result);
                            });
                            zset.diff("zset111", "zset222").onResult((error1, result) -> {
                                log.info("zdiff ret: "+error1+", "+result);
                            });
                            zset.diffWithScores("zset111", "zset222").onResult((error1, result) -> {
                                log.info("zdiff(withScores) ret: "+error1+", "+result);
                            });
                            zset.diffStore("zset333", "zset222", "zset111").onResult((error1, result) -> {
                                log.info("zdiffStore ret: "+error1+", "+result);
                            });
                            zset.incrBy("zset111", 20.5f, "bbb").onResult((error1, result) -> {
                                log.info("zincrby ret: "+error1+", "+result);
                            });
                            zset.inter("zset111", "zset222").onResult((error1, result) -> {
                                log.info("zinter ret: "+error1+", "+result);
                            });
                            zset.interCard("zset111", "zset222").onResult((error1, result) -> {
                                log.info("zintercard ret: "+error1+", "+result);
                            });
                            zset.interStore("zset333", "zset222", "zset111").onResult((error1, result) -> {
                                log.info("zinterstore ret: "+error1+", "+result);
                            });
                            zset.lexCount("zset111", "-", "+").onResult((error1, result) -> {
                                log.info("zlexcount ret: "+error1+", "+result);
                            });
                            zset.mPop(true,"zset111").onResult((error1, result) -> {
                                log.info("zmpop ret: "+error1+", "+result);
                            });
                            zset.mPop(true,2,"zset222").onResult((error1, result) -> {
                                log.info("zmpop(count) ret: "+error1+", "+result);
                            });
                            zset.mScore("zset111", "aaa", "ccc").onResult((error1, result) -> {
                                log.info("zmscore ret: "+error1+", "+result);
                            });
                            zset.popMax("zset111").onResult((error1, result) -> {
                                log.info("zpopmax ret: "+error1+", "+result);
                            });
                            zset.popMax("zset111", 2).onResult((error1, result) -> {
                                log.info("zpopmax(count) ret: "+error1+", "+result);
                            });
                            zset.popMin("zset111").onResult((error1, result) -> {
                                log.info("zpopmin ret: "+error1+", "+result);
                            });
                            zset.popMin("zset111", 2).onResult((error1, result) -> {
                                log.info("zpopmin(count) ret: "+error1+", "+result);
                            });
                            zset.randMember("zset111").onResult((error1, result) -> {
                                log.info("zrandmember ret: "+error1+", "+result);
                            });
                            zset.randMember("zset111", 3, true).onResult((error1, result) -> {
                                log.info("zrandmember(count) ret: "+error1+", "+result);
                            });
                            zset.range("zset111", "2", "3").onResult((error1, result) -> {
                                log.info("zrange ret: "+error1+", "+result);
                            });
                            zset.rangeStore("zset222", "zset111", "2", "-1").onResult((error1, result) -> {
                                log.info("zrangestore ret: "+error1+", "+result);
                            });
                            zset.rank("zset111", "ccc").onResult((error1, result) -> {
                                log.info("zrank ret: "+error1+", "+result);
                            });
                            zset.rankWithScore("zset111", "ccc").onResult((error1, result) -> {
                                log.info("zrank(withScore) ret: "+error1+", "+result);
                            });
                            zset.rem("zset111", "ccc2").onResult((error1, result) -> {
                                log.info("zrem ret: "+error1+", "+result);
                            });
                            zset.remRangeByLex("zset111", "[alpha", "[omega").onResult((error1, result) -> {
                                log.info("zremrangebylex ret: "+error1+", "+result);
                            });
                            zset.remRangeByRank("zset222", 1, -1).onResult((error1, result) -> {
                                log.info("zremrangebyrank ret: "+error1+", "+result);
                            });
                            zset.remRangeByScore("zset222", 60, 80).onResult((error1, result) -> {
                                log.info("zremrangebyscore ret: "+error1+", "+result);
                            });
                            zset.revRange("zset333", 1, 3).onResult((error1, result) -> {
                                log.info("zrevrange ret: "+error1+", "+result);
                            });
                            zset.revRangeWithScores("zset333", 1, 3).onResult((error1, result) -> {
                                log.info("zrevrange(withScores) ret: "+error1+", "+result);
                            });
                            zset.revRangeByLex("zset333", "(g", "[aaa").onResult((error1, result) -> {
                                log.info("zrevrangebylex ret: "+error1+", "+result);
                            });
                            zset.revRangeByScore("zset333", "+inf", "-inf").onResult((error1, result) -> {
                                log.info("zrevrangebyscore ret: "+error1+", "+result);
                            });
                            zset.revRank("zset333", "bbb").onResult((error1, result) -> {
                                log.info("zrevrank ret: "+error1+", "+result);
                            });
                            zset.revRankWithScore("zset333", "bbb").onResult((error1, result) -> {
                                log.info("zrevrank(withScore) ret: "+error1+", "+result);
                            });
                            zset.score("zset333", "bbb").onResult((error1, result) -> {
                                log.info("zscore ret: "+error1+", "+result);
                            });
                            zset.union("2", "zset333", "zset222").onResult((error1, result) -> {
                                log.info("zunion ret: "+error1+", "+result);
                            });
                            zset.unionStore("zset333", "2", "zset333", "zset222").onResult((error1, result) -> {
                                log.info("zunionstore ret: "+error1+", "+result);
                            });
                            //
                            client.set("asdasdasdasd", "111");
                            client.append("asdasdasdasd", "222").onResult((error1, result) -> {
                                log.info("append ret: "+error1+", "+result);
                            });
                            client.getEX("asdasdasdasd", "EX", "60").onResult((error1, result) -> {
                                log.info("getex ret: "+error1+", "+result);
                            });
                            client.setRange("asdasdasdasd", 3, "hehe").onResult((error1, result) -> {
                                log.info("setrange ret: "+error1+", "+result);
                            });
                            client.select(0).onResult((error1, result) -> {
                                log.info("select ret: "+error1+", "+result);
                            });

                            //
                            client.del("abccba123");
                            client.del("abccba321");
                            client.del("abccba123-list2");
                            client.del("setTest111");
                            client.del("setTest222");
                            client.del("setTest333");
                            client.del("asdasd222");
                            client.del("sdfasdff");
                            client.del("zset111");
                            client.del("zset222");
                            client.del("zset333");
                            client.del("asdasdasdasd");
                        }finally {
                            client.release();
                        }
                    });
//            timer.timeout(1000, ()->{
//                RedisClient client = pool.borrow();
//                client.subscribe()
//                        .onResult((error, channel, subsNum) -> {
//                            log.info("subs ret: error="+error+", channel="+channel+", subsNum="+subsNum);
//                        })
//                        .onMessage(((channel, msg) -> {
//                            log.info("recvPubMsg: channel="+channel+", msg="+msg);
//                        }));
////                client.subscribe();//"chAbc111", "chAbc222"
////                client.unsubscribe("111","chAbc111");
//                client.get("dadada").onResult((error, result) -> {
//                    log.info("inSubTest: get, error="+error+", ret="+result);
//                });
////                client.ping();
////                client.psubscribe("chAbd1*", "chAbd2*");
////                client.release();
//
//                timer.timeout(1000, ()->{
//                    RedisClient client2 = pool.borrow();
//                    client2.publish("chAbc111", "publish msg test 1");
//                    client2.publish("chAbc111", "publish msg test 2");
//                    client2.publish("chAbc111", "publish msg test 3");
////                            .onResult((error, result) -> {
////                        log.info("publish ret: error="+error+", ret="+result);
////                    });
//                    client2.release();
//                });
//            });

//            timer.timeout(10000, ()->{
//                log.info("check pool error, error="+pool.lastError()+", time="+pool.lastErrorTime());
//            });
        }
    }

    static class redisClientTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            log.info("redisClientTest1 onStart()");
            String host = "8.210.10.221";
            int port = 11000;
            net.createClient(host,port)
                    .timeout(5000)
                    .codec(ClientCodec.redis())
                    .connect((error, channel) -> {
                        log.info("conn done, error="+error);
                        if(error != null){
                            return;
                        }
                        try {
                            channel.onRead(data -> {
                                RedisRecv recv = (RedisRecv) data;
                                log.info("recvData: "+recv.rawData());
                            }).onClose(()->{
                                log.info("conn gone");
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                        ByteBuf bufSend = sendCmdBuf("auth", "L38(3#j,27qWqE@");
                        channel.write(bufSend);
                        bufSend = sendCmdBuf("auth", "L38(3#j,27qWqE@");
                        channel.write(bufSend);

                        bufSend = sendCmdBuf("get", "test1");
                        channel.write(bufSend);
                        bufSend = sendCmdBuf("ping");
                        channel.write(bufSend);
                        bufSend = sendCmdBuf("keys", "*");
                        channel.write(bufSend);
                    });
        }
    }
    static ByteBuf sendCmdBuf(String cmd, String... args){
        String str = sendCmdStr(cmd, args);
        byte[] bytes = str.getBytes(CharsetUtil.UTF_8);
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        return buf;
    }
    static String sendCmdStr(String cmd, String...args){
        //String cmd = "*2\r\n$4\r\nAUTH\r\n$6\r\nhehehe\r\n";
        StringBuilder sb = new StringBuilder();
        int len = 1 + args.length;
        sb.append("*").append(len).append("\r\n")
                .append('$').append(cmd.length()).append("\r\n").append(cmd).append("\r\n");
        for(int i=0; i<args.length; ++i){
            String arg = args[i];
            sb.append('$').append(arg.length()).append("\r\n").append(arg).append("\r\n");
        }
        return sb.toString();
    }

    static void redisDecodeTest1(){

//        RedisClientWrap c = new RedisClientWrap();
//        CommandFuture<String> f = c.auth("asd");
//        f.onResult((error, result) -> {
//            int n = 1;
//        });
//        c._procReadData("hehehehe");
//        c._procReadData(new Exception("error"));
//        c.sendCommand(RedisCmd.AUTH, "aaa");

        String str1 = "\r\n";
        int nn = str1.length();

        ArrayList<Integer> ls = new ArrayList<>(10);
        ls.add(null);
        LinkedList<Integer> ls2 = new LinkedList<>();
        ls2.offer(null);
        int n1 = ls.size();
        n1 = ls2.size();

        str1 = "中文test";
        n1 = str1.length();
        byte[] bytes1 = str1.getBytes(CharsetUtil.UTF_8);
        n1 = bytes1.length;

        ByteBuf bufCache = Unpooled.buffer(8);
        RedisDecoder dec = new RedisDecoder(bufCache, 11);
        String[] arrStr = {
//                "+OK\r\n",
//                "$0\r\n\r\n",
//                "$-1\r\n",
//                "$6\r\nab\r\nef\r\n",
                "$12\r\n1234567890",
                "12\r\n"
//                ":12345\r\n",
//                "-ERR sth is wrong\r\n"
//                "*-1\r\n",
//                "*0\r\n",
//                "*7\r\n+ok\r\n$-1\r\n:-20\r\n$5\r\nhello\r\n-error\r\n*0\r\n$0\r\n\r\n"
        };
        for(int i=0; i<arrStr.length; ++i){
            String str = arrStr[i];
            byte[] bytes = str.getBytes(CharsetUtil.UTF_8);
            ByteBuf buf = Unpooled.buffer(bytes.length);
            buf.writeBytes(bytes);
            //
            try {
                dec.onRecv(buf);
                int n = 1;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

    }

    static void regexTest1(){
        String regex = "^(http|https)://\\S+$";
        String input = "http://asdasd/test1/test2?a=1&b=2";
        Pattern ptrn = Pattern.compile(regex);
        Matcher matcher = ptrn.matcher(input);
        int grpCnt = matcher.groupCount();
        while (matcher.find()){
            grpCnt = matcher.groupCount();
            String s1 = matcher.group(0);
            int p1 = matcher.start(0);
            String s2 = matcher.group(1);
            int p2 = matcher.start(1);
//            String s3 = matcher.group(2);
//            int p3 = matcher.start(2);
//            int n = 1;
        }

//        input = "https://asdasd/test?a=1&b=2";
        input = "http://ssdfsdf:10086/test1/test2/";
        input = "http://ssdfsdf:10086/test1?a=1&";
//        input = "https://23:h4b";
        try {
            URL url = new URL(input);
            String protocol = url.getProtocol();
            String host = url.getHost();
            int port = url.getPort();
            int portDef = url.getDefaultPort();
            String path = url.getPath();
            String query = url.getQuery();
            String file = url.getFile();
            String ref = url.getRef();
            String userInfo = url.getUserInfo();
            int n = 1;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        input = "http://ssdfsdf:10086/test1?a=1&b=2&c";
        input = "a=1&b=2&c";
        input = "";
        QueryStringDecoder decQuery = new QueryStringDecoder(input, true);
        String str1 = decQuery.path();
        Map<String, List<String>> map = decQuery.parameters();

        QueryStringEncoder encQuery = new QueryStringEncoder(input);
        encQuery.addParam("c","3");
        str1 = encQuery.toString();
        try {
            URI uri = encQuery.toUri();
            int n = 1;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        int n = 1;

    }

    static class RawServerTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            log.info("RawServerTest1 onStart()");
            net.createServer(10086)
                    .listen((error, channel) -> {
                        log.info("listenRet, error="+error);
                    }).onAccept(channel -> {
                        log.info("conn in");
                try {
                    channel.onRead(data -> {
                        TcpRawRecv raw = (TcpRawRecv) data;
                        ByteBuf buf = raw.rawBuf();
                        String str = (String) buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);
                        log.info("recvData: "+data+", bufLen="+buf.readableBytes()+", str="+str);

                        ByteBuf bufSend = Unpooled.buffer(str.length());
                        bufSend.writeCharSequence(str, CharsetUtil.UTF_8);
                        channel.write(bufSend);
                    }).onClose(()->{
                        log.info("conn gone");
                    });
                } catch (StatusIllegalException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    static class RawClientTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            log.info("RawClientTest1 onStart()");

            String host = "8.210.10.221";
            int port = 11000;
            net.createClient(host, port)
                    .timeout(5000)
                    .codec(ClientCodec.redis())
                    .connect((error, channel) -> {
                        log.info("connRet: error="+error);
                        if(error != null){
                            return;
                        }
                        String cmd = "*2\r\n$4\r\nAUTH\r\n$6\r\nhehehe\r\n";
                        ByteBuf bufSend = Unpooled.buffer(cmd.length());
                        bufSend.writeCharSequence(cmd, CharsetUtil.UTF_8);
                        channel.write(bufSend);
                        try {
                            channel.onRead(data -> {
                                TcpRawRecv recv = (TcpRawRecv) data;
                                ByteBuf buf = recv.rawBuf();
                                String str = (String) buf.getCharSequence(buf.readerIndex(), buf.readableBytes(), CharsetUtil.UTF_8);
                                log.info("recvData: "+data+", bufLen="+buf.readableBytes()+", str="+str);
                            }).onClose(()->{
                                log.info("conn gone");
//                                ctx.process.exit();
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                    });

        }
    }

    static class WsOriTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            log.info("WsOriTest1 onStart()");

            net.createServer(10086)
                    .codec(ServerCodec.websocket().uri("/ws"))
                    .listen((error, channel) -> {
                        log.info("listen result: error=" + error);
                    })
                    .onAccept(channel -> {
                        log.info("conn in, " + channel);
                        try {
                            channel.onError(error -> {
                                log.error("channel error: " + error);
                            }).onClose(()->{
                                log.info("conn gone, " + channel);
                            }).onRead(data -> {
                                WebsocketFrame frame = (WebsocketFrame) data;
                                log.info("recvData, "+frame.text());
                                WebSocketFrame rsp = net.websocket().createTextFrame("echo from server");
                                channel.writeThenClose(rsp);
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    static class HttpOriTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            log.info("NetTest1 onStart()");

            net.createServer(10086)
                    .codec(ServerCodec.http())
                    .listen((error, channel) -> {
                        log.info("listen result, error=" + error);
                    })
                    .onAccept(channel -> {
                        log.info("conn in, " + channel);
                        try {
                            channel.onError(error -> {
                                log.info("decode error: "+error);
                            }).onClose(()->{
                                log.info("conn gone, " + channel);
                            }).onRead(data -> {
                                HttpRequestRecv req = (HttpRequestRecv) data;
                                log.info("recvReq: " + req.uri());
                                HttpResponseSend rsp = net.http().createReponse(200, req, channel);
                                channel.writeThenClose(rsp);
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    static class WsClientTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;

            String host = "";
            int port = 10086;
            net.createClient(host,port)
                    .codec(ClientCodec.websocket().uri("/ws"))
                    .connect((error, channel) -> {
                        if(error != null){   // conn failed
                            log.error("conn failed: "+error);
                            return;
                        }
                        try {
                            channel.onRead(data -> {
                                log.info("onRead: "+data);
                            });
                            channel.onClose(()->{
                                log.info("conn gone");
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                        log.info("will send frame");
                        WebSocketFrame frame = net.websocket().createTextFrame("heheda");
                        channel.write(frame);
                    });

        }
    }

    static class HttpClientTest2 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Net net = ctx.net;
            Logger log = ctx.logger;

            log.info("HttpClientTest2 onStart()");

            String url = "https://www.baidu.com";
            net.http().createClient(url)
//                    .connTimeout(5000)
                    .onRequest((error, req) -> {
                        log.info("onRequest, error="+error);
                        if(error != null){
                            return;
                        }
//                        req.addHeader("key1", "val1");
                    })
                    .query((error, rsp) -> {
                        log.info("onResponse, error="+error);
                        if(error != null){
                            return;
                        }
                        log.info("rsp: "+rsp.contentAsString());
                    })
                    .onClose(()->{
                        log.info("conn gone");
                    });

        }
    }

    static class HttpClientTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            log.info("HttpClientTest1 onStart()");
            net.createClient(
//                    "www.acbev35q.dqK",
                    "www.baidu.com",
//                    "14.119.104.254",
//                    "114.119.104.254",
                    443)
                    .codec(ClientCodec.http())
                    .timeout(5000)
                    .sslBegin().end()
                    .connect(((error, channel) -> {
                        log.info("conn ret, error="+error);
                        if(error != null){
                            return;
                        }
                        try {
                            channel.onRead(data -> {
                                HttpResponseRecv rsp = (HttpResponseRecv) data;
//                                NodeContext ctx1 = NodeContext.currentContext();
                                ctx.logger.info("recvRsp: status=" + rsp.status()+", contentStr="+rsp.contentAsString());
                                channel.close();
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                        try {
                            channel.onClose(()->{
                                log.info("conn gone");
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                        HttpRequestSend req = net.http().createRequest("/");
                        req.addHeader("key1", "val1");
                        channel.write(req);
                    }));
        }
    }

    //
    static class WsTest2 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            Timer timer = ctx.timer;
            //
//            int workerNum = 4;
//            List<Long> lsWorker = new ArrayList<>();
//            for(int i=0; i<workerNum; ++i){
//                long pid = ctx.process.fork(SendChannelTest2.class);
//                lsWorker.add(pid);
//            }
            //
            WebsocketServer server = net.websocket().createServer();
            server.onConnection(socket -> {
//                NodeContext ctx1 = NodeContext.currentContext();
//                ctx1.logger.info("connIn, " + ctx1.process.name);
                try {
                    socket.onMessage(msg -> {
                        ctx.logger.info("recvMsg: "+msg.text()+", "+ctx.process.name);
                        socket.send("echo from server, "+ msg.text());
//                        socket.sendThenClose("echo from server, bye");
                    });
                } catch (StatusIllegalException e) {
                    e.printStackTrace();
                }
                try {
                    socket.onClose(()->{
                        ctx.logger.info("connGone, " + ctx.process.name);
                    });
                } catch (StatusIllegalException e) {
                    e.printStackTrace();
                }
            })
//            .workerChooser(new DefaultWorkerChooser(lsWorker))
            ;
            //
            server.listen(10086, "/ws",
                    error -> {
                        log.info("listen ret: " + ((error==null)?"succ":"failed"));
                    });
        }
    }

    //
    static class WsTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            Timer timer = ctx.timer;
            //
            net.createServer(10086)
                    .codec(ServerCodec.websocket().uri("/ws"))
                    .listen((error, channel) -> {
                        log.info("listen ret: "+(error==null?"succ":"failed"));
                    })
                    .onAccept(channel -> {
                        log.info("newWsConn in");
                        try {
                            channel.onRead((data) -> {
                                WebsocketFrame frame = (WebsocketFrame) data;
                                log.info("ws readData, isText="+frame.isText()+", text="+frame.text());
                                WebSocketFrame rsp = net.websocket().createTextFrame("hello from server");
                                channel.writeThenClose(rsp);
                            });
                            channel.onClose(()->{
                                log.info("wsConn gone");
                            });
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    static class HttpServerTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            Timer timer = ctx.timer;
            //
            log.info("HttpServerTest1 onStart()");
            //
//            int workerNum = 4;
//            List<Long> lsWorker = new ArrayList<>();
//            for(int i=0; i<workerNum; ++i){
//                long pid = ctx.process.fork(SendChannelTest2.class);
//                lsWorker.add(pid);
//            }
//            lsWorker.add(ctx.process.pid());
            //
            HttpServer server = net.http().createServer((req, rsp) -> {
//                NodeContext ctx2 = NodeContext.currentContext();
//                Logger log2 = ctx2.logger;

                boolean keepAlive = req.headers().containsValue("Connection", "Keep-Alive", true);
                Map<String,String> mapParam = req.params();
                log.info("recvReq, uri=" + req.uri()+", method="+req.method()+", keepAlive="+keepAlive+", " + ctx.process.name+", content="+req.contentAsString());
                rsp.setStatus(200);
                rsp.addHeader("key1", "val1");
                rsp.echo("hehe1<br/>");
//                timer.timeout(1000, ()->{
                    rsp.end("hehe2");
//                });

            })
//                    .workerChooser(new DefaultWorkerChooser(lsWorker))
                    ;
            server.listen(10086, error -> {
                if(error != null){
                    log.error("listen failed: " + error);
                }else{
                    log.info("listen succ");
                }
            });
//            server.close();

        }
    }

    //
    static class SendChannelTest1 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Net net = ctx.net;
            Process process = ctx.process;
            long dstWorker = process.fork(SendChannelTest2.class);
            net.createServer(10086)
                    .codec(ServerCodec.http())
                    .listen((error, channel) -> {
                        if(error != null){
                            log.error("listen failed");
                        }else {
                            log.info("listen succ, " + process.name);
                        }
                    })
                    .onAccept(channel -> {
                        log.info("newConn in at "+process.name);
                        log.info("will send channel to "+dstWorker);
                        try {
                            boolean sendSucc = channel.sender()
                                    .onRead((data, channel1) -> {
                                        NodeContext ctx1 = NodeContext.currentContext();
                                        ctx1.logger.info("onRead: "+data + ", "+ctx1.process.name);
                                    })
                                    .onClose(channel1 -> {
                                        NodeContext ctx1 = NodeContext.currentContext();
                                        ctx1.logger.info("onClose, "+ctx1.process.name);
                                    })
                                    .send(dstWorker);
                            if(!sendSucc){
                                channel.close();
                            }
                        } catch (StatusIllegalException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
    static class SendChannelTest2 extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            log.info("SendChannelTest2 onStart(), "+ctx.process.name);
        }
    }

    //
    static class TestEntry extends NodeEntry
    {
        @Override
        public void onStart(NodeContext ctx, Object param) {

            Timer timer = ctx.timer;
            Process process = ctx.process;
            Net net = ctx.net;
            Logger log = ctx.logger;

            log.info("thlWorkerId 2: "+LoopWorker.getThlWorker().pid);

            log.info("TestEntry onStart: " + process.name);
            TimerFuture taskTimeout = ctx.timer.timeout(2000, () -> {
                log.info("onTimeout: "+process.name);
            });

////            taskTimeout.cancel();
//
//            for(int i=0; i<5; ++i){
//                ctx.timer.immediate(() -> {
//                    System.out.println("onImmediate");
//                });
//            }
//            ctx.timer.nextTick(() -> {
//                System.out.println("onNextTick: "+Thread.currentThread().getName());
//            });

//            ctx.timer.nextTick("hehe", userData -> {
//                System.out.println("onNextTickUd, ud="+userData+", "+Thread.currentThread().getName());
//            });
//
            s_tmLastSchedule = s_getTimeMs();
            s_scheduleCnt = 3;
            TimerFuture taskSchedule = ctx.timer.schedule(1000, task -> {
                long tmNow = s_getTimeMs();
                long dlt = tmNow - s_tmLastSchedule;
                System.out.println("onSchedule, dlt=" + dlt + ", off="+(dlt-1000)+", cnt="+s_scheduleCnt);
                s_tmLastSchedule = tmNow;
                if(--s_scheduleCnt == 2){
                    ctx.logger.trace("text from logger trace");
                    ctx.logger.debug("text from logger debug");
                    ctx.logger.info("text from logger info");
                    ctx.logger.warn("Tag1","text from logger warn");
                    ctx.logger.error("text from logger error");
                    ctx.logger.fatal("text from logger fatal");

//                    task.cancel();
//                    ctx.process().exit();
                    //ctx.process().kill(s_forkPid);
//                    ctx.process().killOthers();
                    ctx.process.send(s_forkPid, "hehehe");
                }else if(s_scheduleCnt == 1){
                    task.cancel();

                    timer.timeout(3000,()->{
//                        log.info("will exit");
                        doLog("will exit");
                        process.exit();
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doLog("exit sleep over");
                    });

                    ctx.net.createServer(10086)
                            .soBegin().backLog(1024)
                            .keepAlive(true).tcpNoDelay(true)
                            .sendBuff(2048).recvBuff(2048).end()
                            .codec(ServerCodec.http().reqMaxBytes(1024*64))
                            .listen((err, channel)->{
                                if(err == null){  // no error, succ
                                    ctx.logger.info("server succ, "+Thread.currentThread().getName());
                                }else{  // has error
                                    ctx.logger.error("server failed: "+err+", "+Thread.currentThread().getName());
                                }
                            })
                            .onAccept(channel -> {

                                ctx.logger.info("newConn in: "+Thread.currentThread().getName());


//                                ctx.timer.timeout(()->{
//                                    try {
//                                        channel.onRead((ch,data)->{
//                                            ctx.logger.info("readData");
//                                        });
//                                        channel.onDisconnect((ch)->{
//                                            ctx.logger.info("conn gone");
//                                        });
//                                    } catch (StatusIllegalException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                }, 10000);

//                                try {
//                                    channel.onRead(data->{
//                                        DefaultFullHttpRequest req = (DefaultFullHttpRequest) data;
//                                        DecoderResult decRet = req.decoderResult();
//                                        if(decRet.isSuccess()){
//                                            ctx.logger.info("recvData, succ, "+req+", "+ctx.process.name);
//                                        }else if(decRet.isFailure()){
//                                            ctx.logger.info("recvData, decErr, "+decRet.cause()+", "+ctx.process.name);
////                                            ch.close();
//                                        }else{
//                                            int n = 1;
//                                        }
//                                        FullHttpResponse rsp = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK);
////                                        ch.writeThenClose(rsp);
//                                        channel.write(rsp, error -> {
//                                            ctx.logger.info("write1Ret: err="+error);
//                                        });
//
//                                        ctx.timer.timeout(2000, ()->{
//                                            boolean writeRet = channel.write("hehe", err->{
//                                                ctx.logger.info("writeResult: "+err);
//                                                if(err != null){
//                                                    channel.close();
//                                                }
//                                            });
//                                            ctx.logger.info("writeTestRet: "+writeRet);
//                                        });
//
//                                    });
//                                    channel.onClose(()->{
//                                        ctx.logger.info("conn gone: " + ctx.process.name);
//                                    });
//                                } catch (StatusIllegalException e) {
//                                    e.printStackTrace();
//                                }

//                                ctx.timer.timeout(5000, ()->{
//                                    ctx.logger.info("will close channel");
//                                    ctx.process.exit();
////                                    channel.close();
//                                });

                            });

//                    ctx.timer.timeout(()->{
//                        ctx.logger.info("exit now");
//                        ctx.process.exit();
//                    },5000);


//                    ctx.logger().info("shutdown called");
//                    ctx.shutdown();
                }
            });


            for(int i=0; i<1; ++i){
                s_forkPid = ctx.process.fork(TestEntry2.class, ctx.process.forkParamsBuilder().userData(i).build());
            }

        }

        @Override
        public void onExit(NodeContext ctx) {
            ctx.logger.info("TestEntry onExit, "+ctx.process.name);
        }
    }
    static long s_tmLastSchedule;
    static int s_scheduleCnt;
    static long s_forkPid;
    static class TestEntry2 extends NodeEntry {
        private Object _param;
        public void onStart(NodeContext ctx, Object param){
            _param = param;
            System.out.println("TestEntry2 onStart, param="+_param+", " + Thread.currentThread().getName());
        }

        @Override
        public void onExit(NodeContext ctx) {
            System.out.println("TestEntry2 onExit, param="+_param+", "+Thread.currentThread().getName());
        }

        @Override
        public void onMessage(long pidSrc, Object msg) {
            System.out.println("TestEntry2 onMessage, src="+pidSrc+", msg="+msg+", "+Thread.currentThread().getName());
        }
    }

    //
    static DFHiWheelTimer _timer2;

    static void timerTest2(){
        final ExecutorService poolTimeoutCb = Executors.newFixedThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread th = new Thread(r);
                th.setName("timerTest2-timer-callback");
                return th;
            }
        });
        final Random rand = new Random();
        _timer2 = new DFHiWheelTimer(1, 50, timeout -> poolTimeoutCb.submit(timeout::onTimeout));
        int writeThreadNum = 10;
        ExecutorService writePool = Executors.newFixedThreadPool(writeThreadNum);
        for(int i=0; i<writeThreadNum; ++i){
            writePool.submit(()->{
                for(int j=0; j<100000; ++j){
                    long delay = 100 + rand.nextInt(60000);
                    //delay = 1000;
                    _timer2.add(new Timeout2(delay, s_getTimeMs()), delay, 1);
                }
            });
        }
        ExecutorService tickPool = Executors.newFixedThreadPool(1);
        tickPool.submit(()->{
            while (true){
                _timer2.advanceClock(30000);
            }
        });
    }

    static class Timeout2 implements DFTimeout {
        protected final long delayMs;
        protected final long startTime;
        public Timeout2(long delayMs, long startTime) {
            this.delayMs = delayMs;
            this.startTime = startTime;
        }
        @Override
        public void onTimeout() {
            long tmNow = s_getTimeMs();
            long realDelay = tmNow - startTime;
            int bias = (int)(realDelay - delayMs);
//            if(bias >= 20){
                System.out.println("timeout callback: "+Thread.currentThread().getName()+", realDaley="+realDelay +", reqDelay="+this.delayMs+", bias="+bias);
//            }
        }
    }
    static long s_getTimeMs(){
        return System.nanoTime()/1000000;
    }


    //
    static void waitAccuracyTest(){
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        threadPool.submit(new Runnable() {
            public void run() {
                ReentrantLock lock = new ReentrantLock();
                Condition cond = lock.newCondition();
                lock.lock();
                try {
                    for(int i=0; i<100; ++i){
                        long tmBegin = System.nanoTime();
                        cond.await(100, TimeUnit.MILLISECONDS);
                        long tmEnd = System.nanoTime();
                        System.out.println("tmCost="+((tmEnd - tmBegin)/1000000));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });

    }

    //
    static ExecutorService _threadLog;
    static ExecutorService _threadPoolAdd;
    static ExecutorService _threadPoolTimerCb;
    static DFHiWheelTimerBak _dfTimer;
    static Random _rand;
    static void timerTest1(){
        _threadLog = Executors.newSingleThreadExecutor();
        _threadPoolTimerCb = Executors.newSingleThreadExecutor();
        int producerThreadNum = 16;
        _dfTimer = new DFHiWheelTimerBak(10, 100, new DFHiWheelTimerBak.DFHiWheelTimerCb() {
            public void onTimeout(final DFTimeoutBak cb) {
                _threadPoolTimerCb.submit(new Runnable() {
                    public void run() {
                        cb.onTimeout();
                    }
                });
            }
        });
        Thread thTimer = new Thread(new Runnable() {
            public void run() {
                while (true){
                    _dfTimer.advanceClock(10000);
                }
            }
        });
        thTimer.setName("thread-timer");
        thTimer.start();
        //
        _threadPoolAdd = Executors.newFixedThreadPool(producerThreadNum);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _rand = new Random();
        for(int i=0; i<producerThreadNum; ++i){
            _threadPoolAdd.submit(new Runnable() {
                public void run() {
                    for(int i=0; i<10000; ++i){
                        long tmBegin = getTimeMs();
                        long delay = 50 + _rand.nextInt(1000 * 60 * 3);
                        //long delay = 100;
                        TimeoutTestReq req = new TimeoutTestReq(tmBegin, delay);
                        _dfTimer.add(req, delay);
                    }
                }
            });
        }
    }
    static long getTimeMs(){
        return System.nanoTime()/1000000;
    }

    static class TimeoutTestReq implements DFTimeoutBak {
        public final long startTime;
        public final long delayReq;
        public TimeoutTestReq(long startTime, long delayReq){
            this.startTime = startTime;
            this.delayReq = delayReq;
        }

        public void onTimeout() {
            final long tmNow = getTimeMs();
            // log
            _threadLog.submit(new Runnable() {
                public void run() {
                    long realDelay = tmNow - startTime;
                    long delayOff = realDelay - delayReq;
                    if(Math.abs(delayOff)  >= 20){
                        System.out.println("onTimeout: delayOff="+delayOff+", delayReq="+delayReq+", realDelay=" + realDelay
                                + ", now="+tmNow);
                    }
                }
            });
            // readd task
            _threadPoolAdd.submit(new Runnable() {
                public void run() {
                    long delay = 50 + _rand.nextInt(1000 * 60 * 3);
                    TimeoutTestReq req = new TimeoutTestReq(tmNow, delay);
                    _dfTimer.add(req, delay);
                }
            });
        }
    }

    //
    public static class CacheLineTest1Base
    {
        public long p1,p2,p3,p4,p5,p6;
    }
    //@sun.misc.Contended
    public static class CacheTest1 extends CacheLineTest1Base
    {
        //public long p1,p2,p3,p4,p5,p6,p7;
        public long val = 0;
    }
    static CacheTest1[] s_arrCacheTest;
    static void cacheLineTest(){
        final int loopNum = 1000000000;
        s_arrCacheTest = new CacheTest1[2];
        s_arrCacheTest[0] = new CacheTest1();
        s_arrCacheTest[1] = new CacheTest1();

        Thread th1 = new Thread(new Runnable() {
            public void run() {
                for(int i=0; i<loopNum; ++i){
                    CacheTest1 v = s_arrCacheTest[0];
                    v.val = i;
                    //v.p1 = i;
                }
            }
        });
        Thread th2 = new Thread(new Runnable() {
            public void run() {
                for(int i=0; i<loopNum; ++i){
                    CacheTest1 v = s_arrCacheTest[1];
                    v.val = i;
                    //v.p1 = i;
                }
            }
        });
        long startTime = System.nanoTime();
        th1.start();
        th2.start();
        try {
            th1.join();
            th2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        System.out.println("timeCost: " + (endTime-startTime)/100000);
    }

    static void doLog(Object msg){
        System.out.println(msg);
    }
}
