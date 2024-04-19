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
import org.neptune.transport.Connection;
import org.neptune.transport.NettyConnection;
import org.neptune.transport.SocketChannelFactoryProvider;
import org.neptune.common.UnresolvedAddress;
import org.neptune.transport.handler.ConnectionWatchDog;
import org.neptune.transport.handler.ConnectorIdleTriggerHandler;
import org.neptune.transport.handler.IdleStateChecker;
import org.neptune.transport.handler.ResponseHandler;
import org.neptune.transport.processor.ConsumerProcessor;
import org.neptune.transport.protocol.ProtocolDecoder;
import org.neptune.transport.protocol.ProtocolEncoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

/**
 * org.neptune.rpc.transportLayer - NettyTcpConnector
 * <p>
 * ProtocolEncoder -> IdleStateChecker                                                                           (出方向)
 * Client                                                                                                                                   server
 * ResponseHandler <-  ProtocolDecoder  <-  ConnectorIdleStateTrigger <- IdleStateChecker <- ConnectionWatchdog  (入方向)
 *
 * @author tony-is-coding
 * @date 2021/12/16 16:19
 */
public class NettyTcpConnector extends NettyConnector {

    private ConsumerProcessor processor;
    private static final int DEFAULT_CONNECTOR_WORKER_NUM = 10;

    private final ChannelOutboundHandler encoder = new ProtocolEncoder();
    private final ResponseHandler handler = new ResponseHandler();
    private final ChannelInboundHandler connectIdleTriggerHandler = new ConnectorIdleTriggerHandler();



    public NettyTcpConnector(ConsumerProcessor processor) {
        super(DEFAULT_CONNECTOR_WORKER_NUM, false);
        this.processor =  processor;
    }

    public NettyTcpConnector(int workerNum) {
        super(workerNum, false);
    }

    public NettyTcpConnector(boolean isNative) {
        super(DEFAULT_CONNECTOR_WORKER_NUM, isNative);
    }

    public NettyTcpConnector(int workerNum, boolean isNative) {
        super(workerNum, isNative);
    }


    @Override
    public Connection connect(UnresolvedAddress remoteSocketAddress, boolean async) {
        return connect0(remoteSocketAddress, true);
    }

    @Override
    public ConsumerProcessor process() {
        return processor;
    }

    @Override
    public void withProcessor(ConsumerProcessor processor) {
        this.processor = processor;
        handler.withProcess(processor);
    }


    public Connection connect0(final UnresolvedAddress address, boolean async) {
        setOptions();
        final SocketAddress socketAddress = InetSocketAddress.createUnresolved(address.host(), address.port());
        final Bootstrap bs = bootstrap();
        //TODO: 考虑后续用组来管理
        final ConnectionWatchDog watchDog = new ConnectionWatchDog(bs, timer, socketAddress) {
            @Override
            public ChannelHandler[] handlers() {
                return new ChannelHandler[]{
                        this, // in-1
                        // 这里只需要进行写超时检查
                        new IdleStateChecker(timer, 0, 30, 0), // in - 2
                        connectIdleTriggerHandler,
                        // TODO: 空闲处理  in - 3
                        encoder, // out - 1
                        new ProtocolDecoder(), // in - 4
                        handler // in - 5
                };
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
                future.sync(); //README: 同步的创建
            }
        } catch (Throwable t) {
            // todo: throw an runnable exception
            return null;
        }
        // 这里要将 channel 包装成一个Connection, 目的是为了实现连接的异步创建
        return new NettyConnection(future, socketAddress) {
            @Override
            public void setReconnect(boolean reconnect) {
                watchDog.setReconnect(reconnect); // 看门狗代理 reconnect
            }
        };
    }

    @Override
    public void shutdownGracefully() {
        super.shutdownGracefully();
        processor.shutdownGracefully();
    }

    @Override
    protected void doInit() {
        // 初始化设置 channelFactory
        bootstrap().channelFactory(SocketChannelFactoryProvider.connector(socketType()));
    }

    @Override
    protected EventLoopGroup createEventLoopGroup(int nThreads, ThreadFactory factory) {
        /*
        TODO: 基于平台的自适应
            default - NIO
            Linux   - epoll
            MacOS   - Kqueue
        * */
        return new NioEventLoopGroup(nThreads, factory);
    }
}
