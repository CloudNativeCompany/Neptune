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
package org.neptune.core;

import org.neptune.core.consumer.ProxyFactory;
import org.neptune.core.registry.ServiceSubscriber;
import org.neptune.transport.connector.Connector;

/**
 * org.neptune.core.core - Client
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:20
 */
public interface Client {
    /*
        README
         客户端从抽象层面应该理解为一个 配置,工厂 的集合体, 考虑:
            1. 一个连接器,用于创建和管理连接
            2. 代理工厂, 进行class包装
            3. 连接注册中心的能力, 监听与接受服务发布

     */

    String appName();

    ServiceSubscriber serviceSubscriber();

    void watchService(Class<?> itf);

    void watchService(Class<?> itf, String version);

    ServiceSubscriber connectToRegistryServer(String address);

    void connectTo(String address);

    Connector connector();

    // 不应该考虑这个会被多次调用
    <T> ProxyFactory<T> proxy(Class<T> clz);

    void shutdownGracefully();
}
