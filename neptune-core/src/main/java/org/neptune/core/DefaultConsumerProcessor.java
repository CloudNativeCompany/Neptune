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

import org.neptune.transport.processor.ConsumerProcessor;
import org.neptune.transport.ResponsePayload;
import io.netty.channel.Channel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * org.neptune.core.core - DefaultConsumerProcessor
 *
 * @author tony-is-coding
 * @date 2021/12/20 19:09
 */
public class DefaultConsumerProcessor implements ConsumerProcessor {
    private final ThreadPoolExecutor executor;

    public DefaultConsumerProcessor() {
        this.executor = new ThreadPoolExecutor(2,2,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(500));
    }

    @Override
    public void shutdownGracefully() {
        executor.shutdownNow();
    }

    @Override
    public void handlerResponse(Channel channel, ResponsePayload response) throws Exception {
    }

}
