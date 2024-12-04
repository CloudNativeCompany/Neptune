package org.neptune.registry.defaultimpl;

import io.netty.channel.*;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.common.util.LongSequence;
import org.neptune.registry.*;
import org.neptune.transport.RequestPayload;
import org.neptune.transport.ResponsePayload;
import org.neptune.transport.connection.Connection;

import org.neptune.transport.connector.NettyConnector;
import org.neptune.transport.handler.ConnectorIdleTriggerHandler;
import org.neptune.transport.handler.IdleStateChecker;
import org.neptune.transport.handler.ResponseHandler;
import org.neptune.transport.processor.ConnectProcessor;
import org.neptune.transport.protocol.ProtocolDecoder;
import org.neptune.transport.protocol.ProtocolEncoder;
import org.neptune.transport.seialize.KryoSerializer;
import org.neptune.transport.seialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DefaultSubscriber implements ServiceSubscriber {

    private final Connection connection;
    private static final Logger log = LoggerFactory.getLogger(DefaultSubscriber.class);
    private final Serializer messageSerializer;

    public DefaultSubscriber(String addr, int port) {
        messageSerializer = new KryoSerializer();

        HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));
        ChannelHandler[] channelHandlers = {
                new IdleStateChecker(timer, 0, 10, 0), // in - 2
                new ConnectorIdleTriggerHandler(), // in - 3
                new ProtocolEncoder(), // out - 1
                new ProtocolDecoder(), // in - 4
                new ResponseHandler(new ConnectProcessor() {
                    @Override
                    public void handlerResponse(Channel channel, ResponsePayload response) throws Exception {
                        log.info("receive message from: " + channel.remoteAddress().toString());
                    }
                    @Override
                    public void shutdownGracefully() {
                        log.info("shutdown now ....");
                    }
                }) // in - 5
        };
        NettyConnector connector = new NettyConnector(channelHandlers);
        UnresolvedAddress unresolvedAddress = new UnresolvedSocketAddress(addr, port);
        this.connection =  connector.connect0(unresolvedAddress, false);
        log.info("connection to registry succeed...:" + connection.channel().remoteAddress());
    }

    @Override
    public Map<Object, Integer> consumers() {
        return Collections.emptyMap();
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, RegistryNotifier notifier) {
        LongSequence longSequence = new LongSequence();
        RequestPayload payload = new RequestPayload(longSequence.next());
        payload.setSerialTypeCode(messageSerializer.typeCode());
        // 发起订阅消息
        RegistryRequest subscribeRequest = new RegistryRequest();
        subscribeRequest.setBody(serviceMeta);
        subscribeRequest.setType(MessageTye.SubscribeRequest.getCode());
        payload.setBytes(messageSerializer.writeObject(subscribeRequest));
        connection.channel().writeAndFlush(payload).addListener(
                // TODO:加入发送超时监控, writeAndFlush
                (ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) { // success
                        log.info("subscribe connect succeed...");
                    } else { // fail
                        log.info("subscribe connect failure...");
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
