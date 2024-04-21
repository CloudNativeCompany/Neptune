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
package org.neptune.example;

import org.neptune.registry.nacos.NacosServicePublisher;
import org.neptune.rpc.server.DefaultServer;
import org.neptune.rpc.server.Server;
import org.neptune.registry.ServiceMeta;
import org.neptune.rpc.ServiceProvider;
import org.neptune.transport.acceptor.NettyAcceptor;

/**
 * org.neptune.example - SimpleServer
 *
 * @author tony-is-coding
 * @date 2021/12/22 12:28
 */
public class SimpleServer {
    public static void main(String[] args) {
        Server server = null;
        try {

            NacosServicePublisher nacosServicePublisher = new NacosServicePublisher(
                    "127.0.0.1", "8848"
            );
            server = DefaultServer.builder()
                    .serverName("demo-service")
                    .version("1.0.0")
                    .group("test")
                    .port(8001)
                    .servicePublisher(nacosServicePublisher)
                    .build();
            server.start();
        } catch (Exception e) {
        } finally {
            server.shutdownGracefully();
        }
    }
}
