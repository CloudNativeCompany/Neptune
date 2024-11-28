package org.neptune.registry.defaultimpl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;
import org.neptune.registry.ServiceSubscriber;
import org.neptune.registry.SubscribeMessage;
import org.neptune.transport.RequestPayload;
import org.neptune.transport.ResponsePayload;
import org.neptune.transport.connection.Connection;
import org.neptune.transport.connection.NettyConnection;
import org.neptune.transport.handler.ConnectionWatchDog;
import org.neptune.transport.handler.ConnectorIdleTriggerHandler;
import org.neptune.transport.handler.IdleStateChecker;
import org.neptune.transport.handler.ResponseHandler;
import org.neptune.transport.processor.ConsumerProcessor;
import org.neptune.transport.protocol.ProtocolDecoder;
import org.neptune.transport.protocol.ProtocolEncoder;
import org.neptune.transport.seialize.KryoSerializer;
import org.neptune.transport.seialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DefaultSubscriber implements ServiceSubscriber {

    private final Connection connection;

    private final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));

    private static final Logger log = LoggerFactory.getLogger(DefaultSubscriber.class);

    private final Serializer messageSerializer;
    public DefaultSubscriber(String addr, int port) {
        messageSerializer = new KryoSerializer();

        // 1. 初始化, 创建一个连接到注册中心
        // 2. 监听注册中心的事件, 进行相对应的处理
        SocketAddress socketAddress = InetSocketAddress.createUnresolved(addr, port);
        NioEventLoopGroup workers = new NioEventLoopGroup(10, new DefaultThreadFactory("rpc.connect"));
        Bootstrap bootstrap = new Bootstrap().group(workers);

        // 一个针对注册中心的 连接看门狗 -- 断线重连
        final ConnectionWatchDog watchDog = new ConnectionWatchDog(bootstrap, timer, socketAddress) {
            @Override
            public ChannelHandler[] handlers() {
                return new ChannelHandler[]{
                        // 入站看门狗
                        this, // in-1
                        // 这里只需要进行 读/写 超时检查
                        new IdleStateChecker(timer, 0, 30, 0), // in - 2
                        new ConnectorIdleTriggerHandler(), // in - 3
                        new ProtocolEncoder(), // out - 1
                        new ProtocolDecoder(), // in - 4
                        new ResponseHandler(new ConsumerProcessor() {
                            @Override
                            public void handlerResponse(Channel channel, ResponsePayload response) throws Exception {
                                System.out.println("response ");
                            }
                            @Override
                            public void shutdownGracefully() {
                                log.info("shutdown now ....");
                            }
                        }) // in - 5
                };
            }
        };
        ChannelFuture future;
        try {
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(watchDog.handlers());
                }
            });
            future = bootstrap.connect(socketAddress);
            future.sync();
        } catch (Throwable t) {
            throw new RuntimeException("connect error:" + t.getMessage());
        }

        // 这里要将 channel 包装成一个Connection, 目的是为了实现连接的异步创建, 和一些自定义的 观测监控行为
        this.connection = new NettyConnection(future, socketAddress) {
            @Override
            public void setReconnect(boolean reconnect) {
                watchDog.setReconnect(reconnect); // 看门狗代理 reconnect
            }
        };
    }

    @Override
    public Map<Object, Integer> consumers() {
        return Collections.emptyMap();
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, RegistryNotifier notifier) {

        RequestPayload payload = new RequestPayload(100000000L);
        payload.setSerialTypeCode(messageSerializer.typeCode());

        // 发起订阅消息
        SubscribeMessage subscribeMessage = new SubscribeMessage();

        payload.setBytes(messageSerializer.writeObject(subscribeMessage));
        connection.channel().writeAndFlush(payload).addListener(
                // TODO:加入发送超时监控, writeAndFlush
                (ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) { // success
                    } else { // fail
                    }
                });
    }

    @Override
    public void unsubscribe(ServiceMeta serviceMeta) {

    }

    @Override
    public Set<RegistryMeta> serviceList(ServiceMeta serviceMeta) {
        return Collections.emptySet();
    }

    @Override
    public void shutdownGracefully() {

    }
}
