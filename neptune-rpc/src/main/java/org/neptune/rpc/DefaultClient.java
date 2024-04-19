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
package org.neptune.rpc;

import org.neptune.rpc.consumer.ProxyFactory;
import org.neptune.rpc.registry.Registry;
import org.neptune.rpc.registry.RegistryConnectMeta;
import org.neptune.rpc.registry.RegistryMeta;
import org.neptune.rpc.registry.ServiceSubscriber;
import org.neptune.transport.*;
import org.neptune.transport.connector.Connector;
import org.neptune.transport.connector.NettyTcpConnector;

import java.util.concurrent.TimeUnit;

/**
 * org.neptune.rpc.core - DefaultClient
 *
 * @author tony-is-coding
 * @date 2021/12/17 16:51
 */
public class DefaultClient implements Client {

    ServiceSubscriber serviceSubscriber;
    Connector connector;
    private String appName;

    public DefaultClient(String appName, Registry.RegistryType type) {
        this.appName = appName;
        this.connector = new NettyTcpConnector(new DefaultConsumerProcessor());
    }

    public DefaultClient(String appName) {
        this(appName, Registry.RegistryType.DEFAULT);
    }

    public DefaultClient() {
        this("default", Registry.RegistryType.DEFAULT);
    }

    @Override
    public String appName() {
        return appName;
    }

    @Override
    public ServiceSubscriber serviceSubscriber() {
        return serviceSubscriber;
    }

    @Override
    public void watchService(Class<?> itf) {
        watchService0(parseServiceMeta(itf));
    }

    @Override
    public void watchService(Class<?> itf, String version) {
        ServiceMeta serviceMeta = parseServiceMeta(itf);
        serviceMeta.setVersion(version);
        watchService0(serviceMeta);
    }

    @Override
    public Connector connector() {
        return connector;
    }

    @Override
    public void connectTo(String address) {
        UnresolvedAddress unresolvedAddress = Support.resolveSocketAddr(address);
        connector.connect(unresolvedAddress, true);
    }

    @Override
    public <T> ProxyFactory<T> proxy(Class<T> clz) {
        return ProxyFactory.factory(clz)
                .client(this);
    }

    @Override
    public void shutdownGracefully() {
        connector.shutdownGracefully();
        if(serviceSubscriber != null){
            serviceSubscriber.shutdownGracefully();
        }
    }

    @Override
    public ServiceSubscriber connectToRegistryServer(String address) throws Exception {
        serviceSubscriber.connectToRegistryServer(new RegistryConnectMeta() {
            @Override
            public String asConnectString() {
                return address;
            }
        });
        return serviceSubscriber;
    }

    private ServiceMeta parseServiceMeta(Class<?> itf) {
        String serviceName = "ServiceRegistry";
        String version = "1.0.0";
        return new ServiceMeta(serviceName, version);
    }

    private ServiceSubscriber.Watcher watchService0(ServiceMeta serviceMeta) {
        // 将服务加入本地的服务列表?
        ServiceSubscriber.Watcher watcher = new ServiceSubscriber.Watcher() {
            @Override
            public void start() {
                serviceSubscriber.subscribe(serviceMeta, new ServiceSubscriber.RegistryNotifier() {
                    @Override
                    public void notify(RegistryMeta registryMeta, EventType eventType) {
                        // 创建连接?
                        final UnresolvedAddress address = registryMeta.getAddress();
                        if(eventType == EventType.SERVICE_ADDED){
                            // TODO: 创建系列的连接

                        }else if(eventType == EventType.SERVICE_REMOVED){

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
        watcher.start();;
        return watcher;
    }
}
