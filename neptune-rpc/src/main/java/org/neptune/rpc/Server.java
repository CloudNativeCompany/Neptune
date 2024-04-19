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

import org.neptune.registry.ServicePublisher;
import org.neptune.transport.acceptor.Acceptor;


/**
 * org.neptune.rpc.core - Server
 *
 * @author tony-is-coding
 * @date 2021/12/21 18:40
 */
public interface Server {

    Acceptor acceptor();

    ServicePublisher connectToRegistryServer(String address);

    void publish(ServiceProvider serviceProvider);

    void publish(ServiceProvider... serviceProviders);

    void cancelPublish(ServiceProvider serviceProvider);

    void cancelPublish(ServiceProvider... serviceProviders);

    void start();

    ServiceProvider serviceProvider();

    void shutdownGracefully();

    boolean isRunning();
}
