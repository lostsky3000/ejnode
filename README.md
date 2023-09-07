[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE)

# ejnode(Easy-Java-Node)

一句话概括: 

ejnode是一个业务逻辑单线程，非业务功能(io,timer等)多线程的异步消息处理框架，正如nodejs做的那样。

将业务逻辑回归简洁，让开发者摆脱多线程编程的心智负担，但又可以在io等方面享受多线程带来的性能优势。

如其名： 像node一样写简单的java代码。


## ejnode 有哪些特性

- 业务逻辑只用单线程处理 (可扩展多个业务线程来实现负载均衡)

- 每个业务线程对应专属io线程组

- 提供 http,websocket,redis 等常用网络服务及驱动

- 所有io操作无阻塞(基于netty)，全异步


## 示例

[Startup](src/test/java/fun/lib/ejnode/example/Startup.java) 启动一个ejnode


