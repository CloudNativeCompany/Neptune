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
package org.neptune.core.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.neptune.core.Directory;
import org.neptune.core.ServiceMeta;
import org.neptune.core.util.ConcurrentSet;
import org.neptune.core.util.Strings;
import org.neptune.transport.UnresolvedAddress;
import org.neptune.transport.UnresolvedSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * org.neptune.core.registry.impl - ZookeeperServiceSubscriber
 *
 * @author tony-is-coding
 * @date 2021/12/20 14:36
 */
public class ZookeeperServiceSubscriber extends ZookeeperRegistry {

    /*
        Zookeeper 服务订阅端
     */

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceSubscriber.class.getName());

    private final ConcurrentHashMap<Directory, CuratorCache> pathChildrenCaches = new ConcurrentHashMap<>();
    private final ConcurrentMap<UnresolvedAddress, ConcurrentSet<ServiceMeta>> serviceMetaMap = new ConcurrentHashMap<>();

    @Override
    public Map<Object, Integer> consumers() {
        return null;
    }


    @Override
    public void subscribe(ServiceMeta serviceMeta, RegistryNotifier notifier) {
        addSubscribeTo(serviceMeta, notifier);
        subscribe0(serviceMeta);
    }


    private void subscribe0(ServiceMeta serviceMeta) {
        CuratorCache childrenCache = pathChildrenCaches.get(serviceMeta);
        if (childrenCache == null) {
            String directory = getDirectory(serviceMeta);
            CuratorCache newChildrenCache = CuratorCache.builder(configClient, directory).build();
            childrenCache = pathChildrenCaches.putIfAbsent(serviceMeta, newChildrenCache);
            if (childrenCache == null) {
                childrenCache = newChildrenCache;
                childrenCache.listenable().addListener(new CuratorCacheListener() {
                    @Override
                    public void event(Type type, ChildData oldData, ChildData data) {
                        switch (type) {
                            case NODE_CREATED:
                                // 节点新增
                                RegistryMeta registerMeta = parseRegisterMeta(data.getPath());
                                UnresolvedAddress address = registerMeta.getAddress();
                                ServiceMeta sm = registerMeta.getServiceMeta();
                                ConcurrentSet<ServiceMeta> serviceMetaSet = getServiceMeta(address);
                                serviceMetaSet.add(sm);
                                onRegistryChanged(
                                        sm,
                                        registerMeta,
                                        RegistryNotifier.EventType.SERVICE_ADDED
                                );
                                break;
                            case NODE_DELETED:
                                // 节点删除;
                        }
                    }
                });

                try {
                    childrenCache.start();
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Subscribe {} failed, {}.", directory, e);
                    }
                }
            } else {
                newChildrenCache.close();
            }
        }
    }

    private RegistryMeta parseRegisterMeta(String data) {
        /*
            data = path:
            形如: /neptune/provider/{group}/{serviceName}/{version}/{host}:{port}:{weight}
         */
        String[] array0 = Strings.split(data, '/');
        String[] array1 = Strings.split(array0[5], ':');  // {host}:{port}:{weight}

        ServiceMeta meta = new ServiceMeta(array0[2],array0[3],array0[4]);
        UnresolvedAddress address = new UnresolvedSocketAddress(array1[0], Integer.parseInt(array1[1]));
        RegistryMeta registryMeta = new RegistryMeta();

        registryMeta.setServiceMeta(meta);
        registryMeta.setAddress(address);
        registryMeta.setWight(Integer.parseInt(array1[2]));
        return registryMeta;
    }

    private ConcurrentSet<ServiceMeta> getServiceMeta(UnresolvedAddress address) {
        ConcurrentSet<ServiceMeta> serviceMetaSet = serviceMetaMap.get(address);
        if (serviceMetaSet == null) {
            ConcurrentSet<ServiceMeta> newServiceMetaSet = new ConcurrentSet<>();
            serviceMetaSet = serviceMetaMap.putIfAbsent(address, newServiceMetaSet);
            if (serviceMetaSet == null) {
                serviceMetaSet = newServiceMetaSet;
            }
        }
        return serviceMetaSet;
    }

    @Override
    public void unsubscribe(ServiceMeta serviceMeta) {
    }

    @Override
    protected void fireOnReconnectToZk(CuratorFramework client, ConnectionState newState) {

    }


}
