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

import org.neptune.transport.Directory;
import org.neptune.core.ServiceMeta;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * org.neptune.core.registry - AbstractRegistry
 *
 * @author tony-is-coding
 * @date 2022/5/6 15:05
 */
public abstract class AbstractRegistry implements ServiceSubscriber, ServicePublisher {
    /*
        README: 需要实现的功能:
            1. 服务注册
               异步化
               成功事件通知
            2. 服务订阅
                订阅推送
            3. 注册中心断开重连后业务逻辑
     */

    // 服务端 注册的服务集合
    private final ConcurrentHashMap<Directory, RegistrySet> registeredSets = new ConcurrentHashMap<>();
    // 订阅的服务列表
    private final ConcurrentHashMap<Directory, CopyOnWriteArraySet<RegistryNotifier>> subscribeListeners = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<Directory> subscribedServices = new CopyOnWriteArraySet<>();

    protected Set<Directory> getSubscribeSet() {
        return subscribedServices;
    }

    protected synchronized void addSubscribeTo(ServiceMeta meta, RegistryNotifier listener) {
        subscribedServices.add(meta);
        CopyOnWriteArraySet<RegistryNotifier> notifiers = subscribeListeners.get(meta);
        if(notifiers == null){
            notifiers = new CopyOnWriteArraySet<>();
            notifiers.add(listener);
            subscribeListeners.put(meta,notifiers);
        }
    }

    // 注册状态变更时: 新增服务 或者 删除服务时 通知
    protected final void onRegistryChanged(final ServiceMeta serviceMeta, RegistryMeta registryMeta, RegistryNotifier.EventType evType) {
        RegistrySet set = registeredSets.get(serviceMeta);

        if (set == null) {
            RegistrySet newSet = new RegistrySet();
            set = registeredSets.putIfAbsent(serviceMeta, newSet);
            if (set == null) {
                set = newSet;
            }
        }
        final ReentrantReadWriteLock.WriteLock lock = set.lock.writeLock();
        try {
            lock.lock();
            if (evType == RegistryNotifier.EventType.SERVICE_ADDED) {
                set.holder.add(registryMeta);
            } else if (evType == RegistryNotifier.EventType.SERVICE_REMOVED) {
                set.holder.remove(registryMeta);
            }
        } finally {
            lock.unlock();
        }

        CopyOnWriteArraySet<RegistryNotifier> registryNotifiers = subscribeListeners.get(serviceMeta);
        for (RegistryNotifier notifier : registryNotifiers) {
            notifier.notify(registryMeta, evType);
        }

    }

    private static final class RegistrySet {
        private final Set<RegistryMeta> holder = new HashSet<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    }


    @Override
    public void connectToRegistryServer(String connectString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdownGracefully() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register(RegistryMeta meta, RegisterListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregister(RegistryMeta meta, RegisterListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register(RegistryMeta meta) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregister(RegistryMeta meta) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Object, Integer> consumers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, RegistryNotifier notifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unsubscribe(ServiceMeta serviceMeta) {
        throw new UnsupportedOperationException();
    }
}
