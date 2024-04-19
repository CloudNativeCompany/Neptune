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
package org.neptune.registry;


import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * org.neptune.rpc.core - ServiceSubscriber
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:11
 */
public interface ServiceSubscriber extends Registry{

    /**
     * List all consumer's info.
     */
    Map<Object, Integer> consumers();

    /**
     * Subscribe a service from registry server.
     */
    void subscribe(ServiceMeta serviceMeta, RegistryNotifier notifier);

    void unsubscribe(ServiceMeta serviceMeta);

    enum RegisterState {
        PREPARE,
        DONE
    }

    /**
     * 服务注册通知器
     */
    interface RegistryNotifier {
        void notify(RegistryMeta registryMeta, EventType eventType);

        enum EventType {
            SERVICE_ADDED,
            SERVICE_REMOVED
        }
    }

    /**
     * 服务监听器
     */
    interface Watcher {
        void start();

        void waitForAvailable();

        void waitForAvailable(long timeout, TimeUnit timeUnit);
    }

}
