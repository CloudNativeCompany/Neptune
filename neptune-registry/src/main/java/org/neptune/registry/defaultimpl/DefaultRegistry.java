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

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.common.util.ConcurrentSet;
import org.neptune.registry.*;
import org.neptune.transport.HeartBeatPayload;
import org.neptune.transport.RequestPayload;
import org.neptune.transport.ResponsePayload;
import org.neptune.transport.Status;
import org.neptune.transport.connection.Connection;
import org.neptune.transport.handler.*;
import org.neptune.transport.processor.AcceptProcessor;
import org.neptune.transport.protocol.ProtocolDecoder;
import org.neptune.transport.protocol.ProtocolEncoder;
import org.neptune.transport.seialize.Serializer;
import org.neptune.transport.seialize.SerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * org.neptune.rpc.core - DefaultServiceSubscriber
 * 服务订阅者, 接受服务端订阅信息
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:16
 */
public class DefaultRegistry extends AbstractRegistry {

    /*
        Map
        RegistryMeta =>  SubscribeList<Connection> connection
     */

    ConcurrentHashMap<RegistryMeta, ConcurrentSet<Connection>> listener = new ConcurrentHashMap<>();

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
        AcceptProcessor processor = new AcceptProcessor() {
            @Override
            public void handleRequest(Channel channel, RequestPayload request) throws Exception {
                log.info("receive a message from remote: " + channel.remoteAddress());
                Serializer serializer = SerializerFactory.getSerializer(Serializer.SerializerType.parse(request.getSerialTypeCode()));
                SubscribeRequest subscribeRequest = serializer.readObject(request.getBytes(), 0
                        ,request.getBytes().length , SubscribeRequest.class);
                log.info("subscribeMessage_info:{}",JSON.toJSONString(subscribeRequest));
                // TODO: 2024/12/2  handler registry

                ResponsePayload payload = new ResponsePayload(request.getXid());
                payload.setStatus(Status.OK.value());
                payload.setSerialTypeCode(request.getSerialTypeCode());
                SubscribeResponse response = new SubscribeResponse();
                response.setCode(1);
                payload.setBytes(serializer.writeObject(response));

                channel.writeAndFlush(payload).addListener(
                        // TODO:加入发送超时监控, writeAndFlush
                        (ChannelFutureListener) cf -> {
                            if (cf.isSuccess()) { // success
                                log.info("subscribe response succeed...");
                            } else { // fail
                                log.info("subscribe response comm failure...");
                            }
                        });
            }

            @Override
            public void handleException(Channel channel, RequestPayload request, Status status, Throwable cause) {
                log.error("handleException:" + ExceptionUtil.getStackTrace(cause));
            }

            @Override
            public void shutdownGracefully() {

            }
        };


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
                                new ChannelOutboundHandlerAdapter(){
                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        log.error("exceptionCaught:" + ExceptionUtil.getStackTrace(cause));
                                    }
                                },
                                new FlushConsolidationHandler(5, true), // 合并发送, 每5次之后再进行一次真正的网络发发送
                                new IdleStateChecker(timer, 5, 5, 60),
                                new AcceptorIdleTriggerHandler(),
                                new ProtocolDecoder(),
                                new ProtocolEncoder(),
                                new ChannelInboundHandlerAdapter(){
                                    // todo: handler action messages
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        if(msg instanceof RequestPayload){
                                            RequestPayload request = (RequestPayload) msg;
                                            processor.handleRequest(ctx.channel(), request);
                                        }else if(msg instanceof HeartBeatPayload){
                                            HeartBeatPayload heartBeat = (HeartBeatPayload) msg;
                                            log.info("receive a heartbeat from remote:{}", ctx.channel().remoteAddress());
                                        }
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        log.error("exceptionCaught:" + ExceptionUtil.getStackTrace(cause));
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
