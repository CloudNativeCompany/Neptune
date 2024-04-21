## Neptune

> Neptune 是一个程序员的分布式系统研发的起点

### 快速使用

默认情况下需要使用注册中心,  测试代码以nacos 为例

- client
```java
public class SimpleClient {
    public static void main(String[] args) {

        Client client = DefaultClient.builder()
                .clientAppName("hello-client")
                .serviceSubscriber(new NacosServiceSubscriber("127.0.0.1", "8848"))
                .connector(new NettyConnector(new DefaultConsumerProcessor()))
                .build();
        try{
            Service service = client
                    .proxy(Service.class)
                    .newInstance();
            for (int i = 0; i < 500; i++) {
                long start = System.currentTimeMillis();
                service.call("hello world" + i);
                System.out.println("第" + i + "次调度, 耗时" + (System.currentTimeMillis() - start));
            }
        }
        finally {
            client.shutdownGracefully();
        }

    }
}
```
- server
```java
public class SimpleServer {
    public static void main(String[] args) {
        Server server = null;
        try {

            NacosServicePublisher nacosServicePublisher = new NacosServicePublisher(
                    "127.0.0.1", "8848"
            );
            server = DefaultServer.builder()
                    .serverName("demo-service")
                    .version("1.0.0")
                    .group("test")
                    .port(8001)
                    .servicePublisher(nacosServicePublisher)
                    .build();
            server.start();
        } catch (Exception e) {
        } finally {
            server.shutdownGracefully();
        }
    }
}
```

### 文档 & 参考
- [功能完成进度](./docs/project-schedule.md)
- [设计文档](./docs/design-reference.md)
- [系统架构](./docs/design.md)
- [关于优化的思考](./docs/optimizer-thinking.md)

### 参与贡献
1. fork 项目到自己的仓库
2. 分支规范
3. 提交代码
4. 新建Pull Request
5. 经过项目维护人员code review通过后, 可合并到主分支 