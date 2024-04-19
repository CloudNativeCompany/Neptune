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

import org.neptune.rpc.Client;
import org.neptune.rpc.DefaultClient;

/**
 * org.neptune.example - AutoClient
 *
 * @author tony-is-coding
 * @date 2021/12/17 20:26
 */
public class SyncClient {
    public static void main(String[] args) {
        Client client = new DefaultClient();
//        client.connectToRegistryServer("127.0.0.1:8080");
        client.connectTo("127.0.0.1:8080");
        try{
            Service service = client
                    .proxy(Service.class)
                    .newInstance();
            String result = service.call("hello world");
        }
        finally {
            client.shutdownGracefully();
        }

    }
}
