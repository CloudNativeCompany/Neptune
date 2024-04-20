#### 1. 服务级别线程池隔离
#### 2. 同步/异步调用 方案
#### 3.如何进行性能压榨? --几个思路
- 服务端方法执行优化 
    - 如何取代反射的方法调用? 
- 序列化/反序列化 优化
    - 在业务线程中序列化/反序列化, 避免占⽤IO线程
       - 序列化/反序列化占⽤数量极少的IO线程时间⽚ 
       - 反序列化常常会涉及到Class的加载, loadClass有一把锁竞争严重(可通过JMC观察一下) 
    - 选择⾼效的序列化/反序列化框架(kryo/protobuf/protostuff/hessian/fastjson/…) 
    https://github.com/eishay/jvm-serializers/wiki 
    
    - 选择只是第一步, 它(序列化框架)做的不好的, 去扩展和优化 
       - 传统的序列化/反序列化+写⼊/读取⽹络的流程 
        java对象--> byte[] -->堆外内存 / 堆外内存--> byte[] -->java对象
    - 优化思路 
        省去byte[]环节, 直接读/写 堆外内存, 这需要扩展对应的序列化框架
        
#### 4. IO线程绑定 
#### 5. 客户端阻塞调用成为瓶颈? 如何优化 
#### 6. 尽快释放IO线程去做他该做的事情, 尽量减少线程上下文切换
- IO 线程减少CPU的持有?
- 分池这个时候体现出作用来了

#### 7.可扩展性设计(序列化框架、注册中心、监控中心)
- Java SPI 
- Spring Bean Aware？ 
- class scan 参考Mybatis Interceptor 的实现? 