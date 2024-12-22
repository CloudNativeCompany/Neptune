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

import lombok.extern.slf4j.Slf4j;
import org.neptune.registry.defaultimpl.DefaultSubscriber;
import org.neptune.rpc.client.Client;
import org.neptune.rpc.client.DefaultClient;
import org.neptune.transport.connector.NettyConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * org.neptune.example - AutoClient
 *
 * @author tony-is-coding
 * @date 2021/12/17 20:26
 */
@Slf4j
public class SimpleClient {
    private final static Logger logger = LoggerFactory.getLogger(SimpleClient.class);


    public static void main(String[] args) {
        Client client = DefaultClient.builder()
                .clientAppName("hello-client")
                .serviceSubscriber(new DefaultSubscriber("127.0.0.1", 8001))
                .build();
        try{
            Service service = client
                    .proxy(Service.class)
                    .newInstance();
            for (int i = 0; i < 500; i++) {
                long start = System.currentTimeMillis();
                String result = service.call("hello world" + i);
                logger.info("第{}次调度,结果:{}, 耗时 {} ms", i,(System.currentTimeMillis() - start), result );
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            client.shutdownGracefully();
        }

    }
}
