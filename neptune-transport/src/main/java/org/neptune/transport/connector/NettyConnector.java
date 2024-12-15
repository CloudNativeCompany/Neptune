/*
 * Copyright (c) 2022 The Neptune Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neptune.transport.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.neptune.common.UnresolvedAddress;
import org.neptune.transport.SocketChannelFactoryProvider;
import org.neptune.transport.connection.Connection;
import org.neptune.transport.connection.ConnectionGroup;
import org.neptune.transport.connection.NettyConnection;
import org.neptune.transport.handler.ConnectionWatchDog;
import org.neptune.transport.processor.ConnectProcessor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import static org.neptune.transport.SocketChannelFactoryProvider.SocketType;

/**
 * org.neptune.rpc.transportLayer - NettyConnector
 * 抽象的 netty 连接器
 * 1. 进行 netty bootstrap 初始化能力
 * 2.
 *
 * <p>
 * ProtocolEncoder -> IdleStateChecker                                                                           (出方向)
 * Client                                                                                                                                   server
 * ResponseHandler <-  ProtocolDecoder  <-  ConnectorIdleStateTrigger <- IdleStateChecker <- ConnectionWatchdog  (入方向)
 *
 * @author tony-is-coding
 * @date 2021/12/16 1:13
 */
public class NettyConnector implements Connector {

    // timer, 主要是做超时重连的, 但我认为是任意层面/协议 的重连都需要 timer 计时器
    protected final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));

    // netty
    private final Bootstrap bootstrap;
    private final EventLoopGroup workers;
    private final SocketType socketType;

    private final ChannelHandler[] channelHandlers;
    private static final int DEFAULT_CONNECTOR_WORKER_NUM = 4;

    private final ConcurrentHashMap<UnresolvedAddress, ConnectionGroup> serviceConnectGroup = new ConcurrentHashMap<>(16);

    public NettyConnector(ChannelHandler[] channelHandlers) {
        this.channelHandlers = channelHandlers;
        this.socketType = SocketChannelFactoryProvider.socketType(false);
        this.workers = createEventLoopGroup(DEFAULT_CONNECTOR_WORKER_NUM, new DefaultThreadFactory("rpc.connect"));
        this.bootstrap = new Bootstrap().group(workers);
        doInit();
    }

    public SocketType socketType() {
        return this.socketType;
    }

    public Bootstrap bootstrap() {
        return this.bootstrap;
    }

    @Override
    public Connection connect(UnresolvedAddress remoteSocketAddress, boolean async) {
        return connect0(remoteSocketAddress, true);
    }

    public Connection connect0(final UnresolvedAddress address, boolean async) {
        setOptions();

        final SocketAddress socketAddress = InetSocketAddress.createUnresolved(address.host(), address.port());
        final Bootstrap bs = bootstrap();

        // 连接看门狗 -- 断线重连
        final ConnectionWatchDog watchDog = new ConnectionWatchDog(bs,timer, socketAddress) {
            @Override
            public ChannelHandler[] handlers() {
                List<ChannelHandler> handlers = new LinkedList<>();
                handlers.add(this);
                handlers.addAll(Arrays.asList(channelHandlers));
                return handlers.toArray(new ChannelHandler[0]);
            }
        };
        ChannelFuture future;
        try {
            synchronized (bootstrap()) {
                bs.handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(watchDog.handlers());
                    }
                });
                future = bs.connect(socketAddress);
            }
            if (!async) {
                future.sync();
            }
        } catch (Throwable t) {
            throw new RuntimeException("connect error");
        }

        // TODO: 2024/12/2 这里要将 channel 包装成一个Connection, 目的是为了实现连接的异步创建, 和一些自定义的 观测监控行为
        return new NettyConnection(future, socketAddress) {

            // TODO: 2024/12/2  控制是否需要重连
            @Override
            public void setReconnect(boolean reconnect) {
                watchDog.setReconnect(reconnect); // 看门狗代理 reconnect
            }
        };
    }

    @Override
    public void shutdownGracefully() {
        workers.shutdownGracefully();
    }

    @Override
    public ConnectionGroup getAddressConnects(UnresolvedAddress address) {
        //todo: 这里存在风险 -- 如果很多group创建但是group最终没有生成实际的connect, 那就是无效内存的浪费, 考虑惰性put
        if(!serviceConnectGroup.containsKey(address)){
            synchronized (serviceConnectGroup){
                if(!serviceConnectGroup.containsKey(address)){
                    ConnectionGroup group = new ConnectionGroup(address);
                    serviceConnectGroup.put(address,group);
                }
            }
        }
        return serviceConnectGroup.get(address);
    }

    @Override
    public void removeAddressConnects(UnresolvedAddress address) {
        serviceConnectGroup.remove(address);
    }

    /**
     * 1. 初始化ChannelOption 配置
     * 2. 初始化ChannelFactory 配置
     */
    private void doInit() {
        // 初始化设置 channelFactory
        bootstrap().channelFactory(SocketChannelFactoryProvider.connector(socketType()));
    }

    private EventLoopGroup createEventLoopGroup(int nThreads, ThreadFactory factory) {
        /*
        TODO: 基于平台的自适应
            default - NIO
            Linux   - epoll
            MacOS   - Kqueue
        * */
        return new NioEventLoopGroup(nThreads, factory);
    }

    /*
        配置是作用与每个连接的, 每次创建连接都可以进行设置参数
     */
    private void setOptions() {
        // TODO 公共配置抽象拓展
    }

}
