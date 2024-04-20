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

import org.neptune.rpc.server.DefaultServer;
import org.neptune.rpc.server.Server;
import org.neptune.registry.ServiceMeta;
import org.neptune.rpc.ServiceProvider;

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
            server = new DefaultServer();

//            ServiceProvider provider = server.serviceProvider().
//                    serviceMeta(new ServiceMeta())
//                    .provider(new ServiceImpl())
//                    .interfaceClass(Service.class);
//            server.publish(provider);
            server.start();
        } catch (Exception e) {
        } finally {
            server.shutdownGracefully();
        }
    }
}
