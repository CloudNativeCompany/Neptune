## 一、设计一个分布式服务框架 
![](https://cdn.nlark.com/yuque/0/2022/png/22746802/1650244593183-ba2ee2b0-9dba-44d5-aba5-ff34291dbed3.png#clientId=u3dec0670-37bf-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=ue54fdec7&margin=%5Bobject%20Object%5D&originHeight=497&originWidth=905&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=uc393a45a-cfcf-4b8d-8292-127734a47fb&title=)
### 远程调用的流程 

1. 启动服务端(服务提供者)并发布服务到注册中心 
1. 启动客户端(服务消费者)并去注册中心订阅感兴趣的服务 
1. 客户端收到注册中心推送的服务地址列表 
1. 调用者发起调用, Proxy从服务地址列表中选择一个地址并将请求信息<group, providerName, version>, methodName, args[]等信息序列化为字节数组并通过网络发送到该地址上 
1. 服务端收到收到并反序列化请求信息, 根据<group, providerName, version>从本地服务字典里查找到对应providerObject, 再根据<methodName, args[]>通过反射调用指定方法, 并将方法返回值序列化为字节数组返回给客户端 
1. 客户端收到响应信息再反序列化为Java对象后由 Proxy 返回给方法调用者 

**以上流程对方法调用者是透明的, 一切看起来就像本地调用一样**

### 远程调用客户端图解 
![](https://cdn.nlark.com/yuque/0/2022/png/22746802/1650244593193-7268a718-b5b0-4424-aec1-9c93d17603c4.png#clientId=u3dec0670-37bf-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=ud83374fb&margin=%5Bobject%20Object%5D&originHeight=433&originWidth=927&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u4baecccd-4a46-4fac-a177-443141cb52a&title=)
若是netty4.x的线程模型, IO Thread(worker) —> Map<InvokeId, Future>代替全局Map能更好的避免线程竞争 
### **远程调用服务端图解**
![](https://cdn.nlark.com/yuque/0/2022/png/22746802/1650244593181-0b503a54-fd23-42e9-81ff-306c17bebf39.png#clientId=u3dec0670-37bf-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u1bd522cb&margin=%5Bobject%20Object%5D&originHeight=408&originWidth=981&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u37583fe6-aea3-401e-a550-0857e483f13&title=)
**重要概念**: RPC三元组 <ID, Request, Response> 

### 远程调用传输层设计 
#### 底层传输IO模型选择? NIO vs Netty 
**Java原生NIO缺点:**

- **复杂度高**
   - API复杂难懂, ⼊⻔困难 
   - 粘包/半包问题费神 
   - 需超强的并发/异步编程功底, 否则很难写出⾼效稳定的实现 
- **稳定性差, 坑多且深 **
   - 调试困难, 偶尔遭遇匪夷所思极难重现的bug, 边哭边查是常有的事⼉ 
   - linux下EPollArrayWrapper.epollWait直接返回导致空轮训进⽽导致100% cpu的bug⼀直 

也没解决利索, Netty帮你work around(通过rebuilding selector) 

- **NIO代码实现⽅⾯的⼀些缺点**
   - Selector.selectedKeys() 产⽣太多垃圾 
      - Netty修改了sun.nio.ch.SelectorImpl的实现, 使⽤双数组代替HashSet存储来 selectedKeys 
      - 相⽐_HashSet(_迭代器_, _包装对象等_)_少了⼀些垃圾的产⽣_(help GC) _
      - 轻微的性能收益_(1~2%) _
   - Nio的代码到处是synchronized (⽐如allocate direct buffffer和Selector.wakeup()) 对于allocate direct buffffer, Netty的pooledBytebuf 有前置TLAB(Thread-local allocation buffffer)可有效的减少去竞争锁 
   - wakeup调⽤多了锁竞争严重并且开销⾮常⼤(开销⼤原因: 为了在select线程外跟select 线程通信, linux下⽤⼀对pipe, windows下由于pipe句柄不能放⼊f d_set, 只能委曲求 全⽤两个tcp连接模拟), wakeup调⽤少了容易导致select时不必要的阻塞(如果懵逼了就 直接⽤Netty吧, Netty中有对应的优化逻辑) 
   - Netty Native Transport中锁少了很多 
- **fdToKey映射 **
   - EPollSelectorImpl#f dToKey维持着所有连接的f d(描述符)对应SelectionKey的映射, 是个HashMap 
   - 每个worker线程有⼀个selector, 也就是每个worker有⼀个 fdToKey , 这些 fdToKey ⼤ 致均分了所有连接 
   - 想象⼀下单机hold⼏⼗万的连接的场景, HashMap从默认size=16, ⼀步⼀步rehash... 
- **Selector在linux平台是Epoll LT实现**
   - Netty Native Transport⽀持Epoll ET 
- **Direct Buffffers事实上还是由GC管理**
   - DirectByteBuffffer.cleaner这个虚引⽤负责free direct memory, DirectByteBuffffer 只是个壳⼦, 这个壳⼦如果坚强的活下去熬过新⽣代的年龄限制最终晋升到⽼年代将是⼀ 件让⼈伤⼼的事情… 
   - ⽆法申请到⾜够的direct memory会显式触发GC, Bits.reserveMemory() -> { System.gc() }, ⾸先因为GC中断整个进程不说, 代码中还sleep 100毫秒, 醒了要是发 现还不⾏就OOM 
   - 更糟的是如果你听信了个别<XX优化宝典>谗⾔设置了-XX:+DisableExplicitGC参数, 悲 剧会静悄悄的发⽣... 
   - Netty的UnpooledUnsaf eNoCleanerDirectByteBuf 去掉了cleaner, 由Netty框架维 护引⽤计数来实时的去释放 
#### 什么是Netty? 能做什么? 

- Netty是一个致力于创建高性能网络应用程序的成熟的IO框架 
- 相比较与直接使用底层的Java IO API, 你不需要先成为网络专家就可以基于Netty去构建复杂的网络应用 
- 业界常见的涉及到网络通信的相关中间件大部分基于Netty实现网络层 
#### Netty最佳实践 

- **业务线程池必要性**
   - 业务逻辑尤其是阻塞时间较长的逻辑, 不要占用netty的IO线程, dispatch到业务线程池中去 
- **WriteBufferWaterMark, 注意默认的高低水位线设置(32K~64K), 根据场景适当调整**
- **重写MessageSizeEstimator来反应真实的高低水位线**
   - 默认实现不能计算对象size, 由于write时还没路过任何一个outboundHandler就已经开始计算message size, 此时对象还没有被encode成Bytebuf, 所以size计算肯定是不准确的(偏低) 
- **注意EventLoop#ioRatio的设置(默认50), 这是EventLoop执行IO任务和非IO任务的一个时间比例上的控制**
- **空闲链路检测用谁调度**? 
   - Netty4.x默认使用IO线程调度, 使用eventLoop的delayQueue, 一个二叉堆实现的优先级队列, 复杂度为O(log N), 每个worker处理自己的链路监测, 有助于减少上下文切换, 但是网络IO操作与idle会相互影响 
   - 如果总的连接数小, 比如几万以内, 上面的实现并没什么问题, 连接数大建议用HashedWheelTimer实现一个IdleStateHandler, HashedWheelTimer复杂度为 O(1), 同时可以让网络IO操作和idle互不影响, 但有上下文切换开销 
- **使用ctx.writeAndFlush还是channel.writeAndFlush**? 
   - ctx.write直接走到下一个outbound handler, 注意别让它违背你的初衷绕过了空闲链路检测 
   - channel.write从末尾开始倒着向前挨个路过pipeline中的所有outbound handlers 
- **使用Bytebuf.forEachByte() 来代替循环 ByteBuf.readByte()的遍历操作, 避免rangeCheck()**
- **使用CompositeByteBuf来避免不必要的内存拷贝**
   - 缺点是索引计算时间复杂度高, 请根据自己场景衡量 
- **如果要读一个int, 用Bytebuf.readInt(), 不要Bytebuf.readBytes(buf, 0, 4)**
   - 这能避免一次memory copy (long, short等同理) 
- **配置UnpooledUnsafeNoCleanerDirectByteBuf来代替jdk的DirectByteBuf, 让netty框架基于引用计数来释放堆外内存**
   - io.netty.maxDirectMemory 
      - `< 0` : 不使用cleaner, netty方面直接继承jdk设置的最大direct memory size, (jdk的direct memory size是独立的, 这将导致总的direct memory size将是jdk配置的2倍) 
      - `= 0` : 使用cleaner, netty方面不设置最大direct memory size 
      - `> 0` : 不使用cleaner, 并且这个参数将直接限制netty的最大direct memory size, (jdk的direct memory size是独立的, 不受此参数限制) 
- **最佳连接数**
   - 一条连接有瓶颈, 无法有效利用cpu, 连接太多也白扯, 最佳实践是根据自己场景测试 
- **使用PooledBytebuf时要善于利用 -Dio.netty.leakDetection.level 参数**
   - 四种级别: DISABLED(禁用), SIMPLE(简单), ADVANCED(高级), PARANOID(偏执) 
   - SIMPLE, ADVANCED采样率相同, 不到1%(按位与操作 mask ==128 - 1) 
   - 默认是SIMPLE级别, 开销不大 
   - 出现泄漏时日志会出现”LEAK: ”字样, 请时不时grep下日志, 一旦出现”LEAK: ”立刻改为ADVANCED级别再跑, 可以报告泄漏对象在哪被访问的 
   - PARANOID: 测试的时候建议使用这个级别, 100%采样 
- **Channel.attr(), 将自己的对象attach到channel上**
   - 链法实现的线程安全的hash表, 也是**分段锁**(只锁链表头), 只有hash冲突的情况下才有锁竞争(类似ConcurrentHashMapV8版本) 
   - 默认hash表只有4个桶, 使用不要太任性 
#### 传输层调用图解 
![](https://cdn.nlark.com/yuque/0/2022/png/22746802/1650244593182-6af187fb-8f7f-4bfa-8857-e7bbc1e87099.png#clientId=u3dec0670-37bf-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u174e4b3e&margin=%5Bobject%20Object%5D&originHeight=674&originWidth=1108&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=ufd7fbfc4-2043-4b61-9e2f-2c2bd191aef&title=)
### **传输层协议设计**
#### **协议头设计**
```text
                                        Protocol 
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐ 
     2   │   1   │    1   │     8     │      4      │ 
├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤ 
         │       │        │           │             │ 
│  MAGIC   Sign    Status   Invoke Id    Body Size           Body Content      │ 
         │       │        │           │             │ 
└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘ 
```
 
消息头16个字节定长 
magic:     (short) 0xbabe 
sign:      消息标志位, 低地址4位用来表示消息类型request/response/heartbeat等, 高地址4位用来表示序列化类型 
status:    状态位, 设置请求响应状态 
invokeId:  消息 id, long 类型, 未来jupiter可能将id限制在48位, 留出高地址的16位作为扩展字段 
bodySize:  消息体 body 长度, int 类型 
 
消息体无固定长度 
#### **协议体设计**

- **metadata**: <group, providerName, version> 
- **methodName**
- ~~**parameterTypes**[]~~ 真的需要? 
   - 有什么问题? 
      - 反序列化时ClassLoader.loadClass()潜在锁竞争 
      - 协议体码流大小 
      - 泛化调用多了参数类型 
- 能解决吗? 
   - Java方法静态分派规则参考JLS <Java语言规范> $15.12.2.5 Choosing the Most Specific Method 章节 
- **args**[] 
- 其他: **traceId**, **appName**… 
## 二、一些好的功能与建议 
### **客户端代理**
#### **为什么需要客户端代理？**

- 集群容错 
- 客户端负载均衡 
- 下层网络屏蔽 
#### 怎么创建客户端代理? 

- JDK Proxy 
- javassist 
- cglib 
- asm 
- bytebuddy (推荐的: 创建高效的类 && 友好的API && 高可定制度) 
#### **需要注意**

- 注意拦截toString, equals, hashCode等方法避免远程调用(参考Mybatis实现) 
### **优雅的同步/异步调⽤**

- 往上翻再看看'远程调⽤客户端图解' 
- 思考下如何拿到future? 
### **序列化/反序列化**

- java native 
- kryo 
- protostuff 
- hessian 

问题： 

- 序列化用户可选择性？ 
- 客户端服务端序列化器一致性检测与限定? 
### 可扩展性 

- Java SPI 
- Spring Bean Aware？ 
- class scan 参考Mybatis Interceptor 的实现? 
### **服务级别线程池隔离**
### **责任链模式的拦截器**
### **指标度量(Metrics)**
### **链路追踪**

- OpenTracing 
### **注册中心**
### **流量控制**

- 应用级别(Global) 
- 服务级别(Service) 
### 服务拒绝 
### **负载均衡**

- 加权随机 
- 加权轮询(最大公约数) 
- 最小负载 
- 一致性哈希 

需要注意服务预热 
### **集群容错**

- FailFast 
- Failover 
- FailSafe 
- FailBack 
- Forking 
### 健康检测 
#### 心跳检测

#### 空闲链路检查

- 读写超时都是基于上一次的读或者写而言了, 每次读/写都需要进行超时(空闲)的重置
- channel的基本生命周期
   - register -> active -> inactive -> unregister
- 什么时候需要进行空闲开始时间的重置?
   - xxx?
- 分离空闲检查, 作为connector端,只需要关注写空闲, 作为acceptor端则只需要关注读空闲
- 读写空闲需要分别处理, 所以需要两个不同的handler来拦截 userEventTriggere
   - 读空闲, 代表远端(一般是client端)无消息发送过来, 可以尝试进行连接断开
   - 写空闲, 代表一段时间内未向远端(一般是server端)写入数据, 这时候需要发送心跳消息避免连接被kill
> 心跳消息由客户端发起, 这样能一定程度上减少 server 端的压力(但是这并非完美的, 考虑 server - client 的数量持平, 每个client都连接了n个server)


### 断线重连 
#### 基于Netty的短线重连实现 
netty 的 ChannelInboundHandler#channelInactive 支持连接断开检测, 检测到断开后, 可以尝试重连任务, 重连可以通过 channel.pipeline().fireChannelInactive() 重复触发断开重连事件 
```java
public class ConnectionWatchDog extends ChannelInboundHandlerAdapter { 
    
    private final ReconnectTask task = new ReconnectTask(); 
    private final Bootstrap bootstrap; 
    private final Timer timer; 
    private final ChannelHandler[] handlers; 
    private final SocketAddress remoteAddress; 
    
    private int attempts; 
    
    public ConnectionWatchDog(Bootstrap bootstrap, Timer timer, ChannelHandler[] handlers, SocketAddress remoteAddress) { 
        this.bootstrap = bootstrap; 
        this.timer = timer; 
        this.handlers = handlers; 
        this.remoteAddress = remoteAddress; 
    } 
    
    @Override 
    public void channelActive(ChannelHandlerContext ctx) throws Exception { 
        super.channelActive(ctx); 
    } 
    
    @Override 
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { 
        final boolean needReconnect = task.reconnect; 
        if (needReconnect) { 
            if (attempts < 12) { 
                attempts++; 
            } 
            long timeout = 2L << attempts; // README:  2进制指数避让 重连算法 
            timer.newTimeout(task, timeout, TimeUnit.MILLISECONDS); 
        } 
    } 
    
    public void setReconnect(boolean reconnect) { 
        task.setReconnect(reconnect); 
    } 
     //一个用于处理重连的任务类 
class ReconnectTask implements TimerTask { 
        private volatile boolean reconnect; 
 
        @Override 
        public void run(Timeout timeout) throws Exception { 
            if (!reconnect) { 
                System.out.println("do not need reconnect"); 
                return; 
            } 
            final ChannelFuture future; 
            synchronized (bootstrap) { 
                bootstrap.handler(new ChannelInitializer<Channel>() { 
                    @Override 
                    protected void initChannel(Channel ch) throws Exception { 
                        ch.pipeline().addLast(handlers); 
                    } 
                }); 
                future = bootstrap.connect(remoteAddress); 
            } 
            future.addListener((ChannelFutureListener) f -> { 
                final boolean succeed = f.isSuccess(); 
                if (!succeed) { 
                    // 从头开始再次执行入栈的 channelInactive() 回调 
                    f.channel().pipeline().fireChannelInactive(); 
                } 
            }); 
        } 
        public void setReconnect(boolean reconnect) { 
            this.reconnect = reconnect; 
        } 
    } 
} 
```

#### 几个要点 

- **降频重连减轻服务器压力**
   - 重试次数逐渐增加延迟重连时间; 二进制指数避让? 
## 三、如何进行性能压榨? 
#### 服务端方法执行优化 

- 如何取代反射的方法调用? 
#### **序列化/反序列化**

- **在业务线程中序列化/反序列化, 避免占⽤IO线程**
   - 序列化/反序列化占⽤数量极少的IO线程时间⽚ 
   - 反序列化常常会涉及到Class的加载, loadClass有一把锁竞争严重(可通过JMC观察一下) 
- **选择⾼效的序列化/反序列化框架**(kryo/protobuf/protostuff/hessian/fastjson/…) 

https://github.com/eishay/jvm-serializers/wiki 

- 选择只是第一步, 它(序列化框架)做的不好的, 去扩展和优化 
   - 传统的序列化/反序列化+写⼊/读取⽹络的流程 

java对象--> byte[] -->堆外内存 / 堆外内存--> byte[] -->java对象

- 优化思路 

省去byte[]环节, 直接读/写 堆外内存, 这需要扩展对应的序列化框架 
#### IO线程绑定 
#### 客户端阻塞调用成为瓶颈? 如何优化 
#### **Netty Native Transport & PooledByteBuf Allocator**
减少GC带来的性能波动 
#### **尽快释放IO线程去做他该做的事情, 尽量减少线程上下文切换**

- **IO 线程减少CPU的持有?**
- **分池这个时候体现出作用来了**
