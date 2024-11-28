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
package org.neptune.registry.defaultimpl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.registry.AbstractRegistry;
import org.neptune.registry.ServiceMeta;
import org.neptune.registry.ServiceSubscriber;
import org.neptune.transport.handler.*;
import org.neptune.transport.protocol.ProtocolDecoder;
import org.neptune.transport.protocol.ProtocolEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * org.neptune.rpc.core - DefaultServiceSubscriber
 * 服务订阅者, 接受服务端订阅信息
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:16
 */
public class DefaultRegistry extends AbstractRegistry {

    private final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));

    private static final Logger log = LoggerFactory.getLogger(DefaultRegistry.class);

    private ChannelFuture channelFuture;
    private final String addr;
    private final int port;

    public DefaultRegistry(String addr, int port){
        this.addr = addr;
        this.port = port;
    }

    @Override
    protected void actionAfterRegister() {
    }

    @Override
    protected void actionAfterSubscribe(ServiceMeta serviceMeta, ServiceSubscriber.RegistryNotifier notifier) {
    }

    @Override
    public void shutdownGracefully() throws InterruptedException {
        this.channelFuture.channel().close().sync();
    }

    @Override
    public void startServer() throws InterruptedException {
        UnresolvedSocketAddress socketAddress =  new UnresolvedSocketAddress(addr, port);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);

        NioEventLoopGroup boss = new NioEventLoopGroup(4, new DefaultThreadFactory("neptune-acceptor-boss", Thread.MAX_PRIORITY));
        NioEventLoopGroup worker = new NioEventLoopGroup(12, new DefaultThreadFactory("neptune-acceptor-worker", Thread.MAX_PRIORITY));
        ServerBootstrap bootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(boss, worker)
                .option(ChannelOption.SO_BACKLOG, 128)          // 设置TCP缓冲区
                .childOption(ChannelOption.SO_KEEPALIVE, true); // 保持连接
        bootstrap
                .childHandler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                new FlushConsolidationHandler(5, true), // 合并发送, 每5次之后再进行一次真正的网络发发送
                                new IdleStateChecker(timer, 5, 5, 60),
                                new AcceptorIdleTriggerHandler(),
                                new ProtocolDecoder(),
                                new ProtocolEncoder(),
                                new ChannelInboundHandlerAdapter(){
                                    // todo: handler action messages
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.info("received a message:{}", msg);
                                        super.channelRead(ctx, msg);
                                    }
                                }
                        );
                    }
                });
        log.info("bind port to: {} success!!!", socketAddress.port());
        this.channelFuture = bootstrap.bind(inetSocketAddress).sync();
        channelFuture.channel().closeFuture().addListeners((ChannelFutureListener) cf -> {
            log.warn("registry server closing.... ");
        });
    }
}
