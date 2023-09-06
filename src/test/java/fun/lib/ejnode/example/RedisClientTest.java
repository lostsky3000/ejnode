package fun.lib.ejnode.example;

import fun.lib.ejnode.api.Db;
import fun.lib.ejnode.api.Logger;
import fun.lib.ejnode.api.NodeEntry;
import fun.lib.ejnode.api.Timer;
import fun.lib.ejnode.core.EJNode;
import fun.lib.ejnode.core.NodeContext;
import fun.lib.ejnode.core.db.redis.*;
import fun.lib.ejnode.core.pool.Pool;

public class RedisClientTest {

    public static void main(String[] args){
        EJNode.get()
                .entry(Entry.class)
                .start();
    }

    static class Entry extends NodeEntry{
        @Override
        public void onStart(NodeContext ctx, Object param) {
            Logger log = ctx.logger;
            Timer timer = ctx.timer;
            Db db = ctx.db;

            String host = "127.0.0.1";
            int port = 6379;
            String password = "123456";
            Pool<RedisClient> pool = db.redis().createPool()
                    .connConfig(host, port, password)
                    .connTimeout(10000)
                    .keepAliveInterval(30000)
                    .poolSize(1)
                    .start();
            timer.timeout(3000, ()->{
                RedisClient client = pool.borrow();
                if(client == null){
                    log.error("redis not ready: "+pool.lastError());
                    return;
                }
                clientTest(client);
                pubSubTest(pool.borrow());
            });
        }

        private void clientTest(RedisClient client){
            NodeContext ctx = NodeContext.currentContext();
            Logger log = ctx.logger;
            try {
                client.get("test").onResult((error, result) -> {
                    log.info("get, ret="+result+", error="+error);
                });
                // list
                RedisApiList list = client.apiList();
                list.push("list_1","ele1");
                list.rpop("list_1").onResult((error, result) -> {
                    log.info("rpop, ret="+result+", error="+error);
                });
                // hash
                RedisApiHash hash = client.apiHash();
                hash.mSet("hash1", "key1","val1", "key2", "val2");
                hash.mGet("hash1", "key1","key2").onResult((error, result) -> {
                    log.info("mGet, ret="+result+", error="+error);
                });
                client.del("hash1");
                // set
                RedisApiSet set = client.apiSet();
                set.add("set1", "mem1", "mem2", "mem3");
                set.randMember("set1", 2).onResult((error, result) -> {
                    log.info("randMember, ret="+result+", error="+error);
                });
                client.del("set1");
                // zset
                RedisApiZSet zset = client.apiZSet();
                zset.add("zset1", "80","a1", "95","a2", "75","a3", "99","a4");
                zset.rank("zset1", "a1").onResult((error, result) -> {
                    log.info("rank, ret="+result+", error="+error);
                });
                client.del("zset1");
                // script
                RedisApiScript script = client.apiScript();
                String strLua = "return 'lua test'";
                script.eval(strLua, "0").onResult((error, result) -> {
                    log.info("eval, ret="+result+", error="+error);
                });
                script.scriptLoad(strLua).onResult((error, result) -> {
                    log.info("loadScript, ret="+result+", error="+error);
                });
            }finally {
                client.release();
            }
        }

        private void pubSubTest(RedisClient client){
            NodeContext ctx = NodeContext.currentContext();
            Logger log = ctx.logger;

            client.subscribe("channel1").onSubsResult((error, channel, subsNum) -> {
                log.info("subscribe at "+channel+" succ, channelNum="+subsNum);

                client.unsubscribe("channel1").onUnsubsResult((error1, channel1, subsNum1) -> {

                });
                client.release();
            });
        }
    }
}
