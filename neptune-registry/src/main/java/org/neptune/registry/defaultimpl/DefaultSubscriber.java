package org.neptune.registry.defaultimpl;

import io.netty.channel.*;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.common.util.LongSequence;
import org.neptune.registry.*;
import org.neptune.transport.RequestFuture;
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
import org.neptune.transport.seialize.SerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
                    public void handlerResponse(Channel channel, ResponsePayload responsePayload) throws Exception {
                        log.info("receive message from registry: {}, xid:{}" , channel.remoteAddress().toString(), responsePayload.getXid());
                        Serializer serializer = SerializerFactory.getSerializer(responsePayload.getSerialTypeCode());
                        RegistryResponse response = serializer.readObject(responsePayload.getBytes(), 0 ,
                                responsePayload.getBytes().length , RegistryResponse.class);
                        RegistryRequestFuture.received(channel,responsePayload.getXid(), response);
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
        RequestPayload payload = new RequestPayload(IdGenerator.newId());
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
    public Set<RegistryMeta> serviceList(ServiceMeta serviceMeta) throws Throwable{
        final long xId = IdGenerator.newId();
        RequestPayload payload = new RequestPayload(xId);
        payload.setSerialTypeCode(messageSerializer.typeCode());
        // 发起服务列表查询请求
        RegistryRequest subscribeRequest = new RegistryRequest();
        subscribeRequest.setBody(serviceMeta);
        subscribeRequest.setType(MessageTye.FetchServiceInstance.getCode());
        payload.setBytes(messageSerializer.writeObject(subscribeRequest));

        log.info("trace_Xid_start:{}", xId);
        RegistryRequestFuture requestFuture = new RegistryRequestFuture(connection.channel(), xId);
        connection.channel().writeAndFlush(payload).addListener(
                (ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) { // success
                        requestFuture.onSentSuccess();
                    } else { // fail
                        requestFuture.onSentFailure();
                    }
                });
        RegistryResponse response = requestFuture.response();
        byte code = response.getCode();
        if(RegistryStatus.SUCCESS.value() != code){
            throw new RuntimeException("error code:" + code + "; msg:" + response.getMsg());
        }
        List<InstanceMeta> instances = (List<InstanceMeta>) response.getBody();
        return instances.stream().map(e -> {
            RegistryMeta meta = new RegistryMeta();
            meta.setServiceMeta(serviceMeta);
            meta.setWight(e.getWight());
            meta.setAddress(e.getAddress());
            return meta;
        }).collect(Collectors.toSet());
    }

    @Override
    public void shutdownGracefully() {

    }
}
