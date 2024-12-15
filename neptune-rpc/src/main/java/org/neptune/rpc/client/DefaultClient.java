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
package org.neptune.rpc.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.neptune.common.UnresolvedAddress;
import org.neptune.registry.*;
import org.neptune.rpc.DefaultRequestFuture;
import org.neptune.rpc.RpcResponse;
import org.neptune.rpc.annotation.RpcService;
import org.neptune.transport.ResponsePayload;
import org.neptune.transport.connection.Connection;
import org.neptune.transport.connection.ConnectionGroup;
import org.neptune.transport.connector.Connector;
import org.neptune.transport.connector.NettyConnector;
import org.neptune.transport.handler.ConnectorIdleTriggerHandler;
import org.neptune.transport.handler.IdleStateChecker;
import org.neptune.transport.handler.ResponseHandler;
import org.neptune.transport.processor.ConnectProcessor;
import org.neptune.transport.protocol.ProtocolDecoder;
import org.neptune.transport.protocol.ProtocolEncoder;
import org.neptune.transport.seialize.Serializer;
import org.neptune.transport.seialize.SerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.neptune.common.util.Requires.requireNotNull;

/**
 * org.neptune.rpc.core - DefaultClient
 *
 * @author tony-is-coding
 * @date 2021/12/17 16:51
 */
@Slf4j
public class DefaultClient implements Client {

    private final static Logger logger = LoggerFactory.getLogger(DefaultClient.class);

    private ServiceSubscriber serviceSubscriber;
    private String clientAppName;
    private Connector connector;

    public static DefaultClientBuilder builder() {
        return new DefaultClientBuilder();
    }

    private DefaultClient() {
    }

    @Override
    public String getClientAppName() {
        return clientAppName;
    }

    public Connector getConnector() {
        return connector;
    }

    @Override
    public ServiceSubscriber serviceSubscriber() {
        return serviceSubscriber;
    }

    @Override
    public <T> ProxyFactory<T> proxy(Class<T> clz) {
        ServiceMeta serviceMeta = parseServiceMeta(clz);
        return proxy(clz, serviceMeta, FactoryProxy.defaultFactoryProxy());
    }

    @Override
    public <T> ProxyFactory<T> proxy(Class<T> clz, ServiceMeta serviceMeta) {
        return proxy(clz, serviceMeta,FactoryProxy.defaultFactoryProxy());
    }

    @Override
    public <T> ProxyFactory<T> proxy(Class<T> clz, ServiceMeta serviceMeta, FactoryProxy factoryProxy) {

        watchForServerAvailable(serviceMeta).waitForAvailable();

        return ProxyFactory.factory(clz)
                .serviceMeta(serviceMeta)
                .client(this)
                .factoryProxy(factoryProxy);
    }

    @Override
    public void shutdownGracefully() {
        serviceSubscriber.shutdownGracefully();
        connector.shutdownGracefully();
        if (serviceSubscriber != null) {
            serviceSubscriber.shutdownGracefully();
        }
    }

    @Override
    public <T>  ServiceSubscriber.Watcher watchForServerAvailable(Class<T> clz) {
        return watchForService0(parseServiceMeta(clz));
    }

    @Override
    public  ServiceSubscriber.Watcher watchForServerAvailable(ServiceMeta serviceMeta) {
        return watchForService0(serviceMeta);
    }

    private ServiceMeta parseServiceMeta(Class<?> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            throw new RuntimeException("proxy class must by an interface type");
        }
        RpcService annotation = interfaceClass.getAnnotation(RpcService.class);
        if (annotation == null) {
            throw new RuntimeException("@RpcService annotation needed");
        }

        String appName = annotation.name();
        String appVersion = annotation.version();
        String group = annotation.group();
        return new ServiceMeta(appName, appVersion, group);
    }

    // 这里主要是考虑要不要 确保有足够的可用的连接在进行 支持到RPC调用 -- 需要同步的把异常抛出来
    private ServiceSubscriber.Watcher watchForService0(final ServiceMeta serviceMeta) {
        ServiceSubscriber.Watcher watcher = new ServiceSubscriber.Watcher() {
            @Override
            public void start() {
                try {
                    serviceSubscriber.subscribe(serviceMeta, new ServiceSubscriber.RegistryNotifier() {
                        @Override
                        public void notify(RegistryMeta registryMeta, EventType eventType) {
                            final UnresolvedAddress address = registryMeta.getAddress();
                            //TODO(谭志勇) 2024/12/15: Exception handler?
                            if (eventType == EventType.SERVICE_ADDED) {
                                Connection connect = connector.connect(address, false);
                                connect.addConnectFuture(() -> {
                                    ConnectionGroup group = connector.getAddressConnects(address);
                                    group.addConnect(connect);
                                });
                            } else if (eventType == EventType.SERVICE_REMOVED) {
                                connector.removeAddressConnects(address);
                            }
                        }
                    });
                    Set<RegistryMeta> serviceList = serviceSubscriber.serviceList(serviceMeta);
                    for (RegistryMeta registryMeta : serviceList) {
                        UnresolvedAddress address = registryMeta.getAddress();
                        Connection connect = connector.connect(address, true);
                        connect.addConnectFuture(() -> {
                            ConnectionGroup group = connector.getAddressConnects(address);
                            group.addConnect(connect);
                        });
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void waitForAvailable() {
//                serviceSubscriber.serviceList(serviceMeta);
            }

            @Override
            public void waitForAvailable(long timeout, TimeUnit timeUnit) {
                // TODO: 预留给配置全局超时时间使用的
            }
        };
        watcher.start();
        return watcher;
    }


    public static class DefaultClientBuilder {
        private final DefaultClient innerClient;

        public DefaultClientBuilder() {
            innerClient = new DefaultClient();
            innerClient.clientAppName = "default-client";
        }

        public DefaultClientBuilder clientAppName(String clientAppName) {
            innerClient.clientAppName = clientAppName;
            return this;
        }

        public DefaultClientBuilder serviceSubscriber(ServiceSubscriber serviceSubscriber) {
            innerClient.serviceSubscriber = serviceSubscriber;
            return this;
        }

        public DefaultClientBuilder connector(Connector connector) {
            innerClient.connector = connector;
            return this;
        }

        public DefaultClient build() {
            //TODO(谭志勇) 2024/12/15: optimizer client
            final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));
            ChannelHandler[] channelHandlers = {
                    new IdleStateChecker(timer, 0, 10, 0), // in - 2
                    new ConnectorIdleTriggerHandler(), // in - 3
                    new ProtocolEncoder(), // out - 1
                    new ProtocolDecoder(), // in - 4
                    new ResponseHandler(new ConnectProcessor() {
                        @Override
                        public void handlerResponse(Channel channel, ResponsePayload response) throws Exception {
                            log.info("receive message from: " + channel.remoteAddress().toString());
                            Serializer serializer = SerializerFactory.getSerializer(response.getSerialTypeCode());
                            RpcResponse rpcResponse = serializer.readObject(response.getBytes(), 0,
                                    response.getBytes().length, RpcResponse.class);
                            DefaultRequestFuture.received(channel,response.getXid(), rpcResponse);
                        }
                        @Override
                        public void shutdownGracefully() {
                            log.info("shutdown now ....");
                        }
                    }) // in - 5
            };

            innerClient.connector = new NettyConnector(channelHandlers);
            return innerClient;
        }
    }
}
