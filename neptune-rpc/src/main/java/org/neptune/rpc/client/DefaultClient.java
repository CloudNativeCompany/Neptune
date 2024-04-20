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

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.util.Strings;
import org.neptune.registry.*;
import org.neptune.rpc.annotation.RpcService;
import org.neptune.transport.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.neptune.common.util.Requires.requireNotNull;

/**
 * org.neptune.rpc.core - DefaultClient
 *
 * @author tony-is-coding
 * @date 2021/12/17 16:51
 */
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
    public <T> void watchForServerAvailable(Class<T> clz) {
        watchForService0(parseServiceMeta(clz));
    }

    @Override
    public void watchForServerAvailable(ServiceMeta serviceMeta) {
        watchForService0(serviceMeta);
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

    private ServiceSubscriber.Watcher watchForService0(ServiceMeta serviceMeta) {
        ServiceSubscriber.Watcher watcher = new ServiceSubscriber.Watcher() {

            @Override
            public void start() {
                serviceSubscriber.subscribe(serviceMeta, new ServiceSubscriber.RegistryNotifier() {
                    @Override
                    public void notify(RegistryMeta registryMeta, EventType eventType) {
                        final UnresolvedAddress address = registryMeta.getAddress();
                        if (eventType == EventType.SERVICE_ADDED) {
                        } else if (eventType == EventType.SERVICE_REMOVED) {
                        }
                    }
                });
            }

            @Override
            public void waitForAvailable() {
            }

            @Override
            public void waitForAvailable(long timeout, TimeUnit timeUnit) {

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
            return innerClient;
        }
    }
}
