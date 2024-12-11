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


import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.neptune.transport.RequestFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * org.neptune.rpc.core - DefaultInvokeFuture
 * 通过这个进行阶段通知
 *
 * @author tony-is-coding
 * @date 2021/12/20 17:56
 */
@Slf4j
public class RegistryRequestFuture extends CompletableFuture<RegistryResponse> implements RequestFuture<RegistryResponse> {

    private static final ConcurrentHashMap<Long, RegistryRequestFuture> FUTURE_HOLDER = new ConcurrentHashMap<>(16);
    private static final byte SEND_FAILURE = -1;
    private static final byte SEND_SUCCESS = 1;
    private static final byte SENDING = 0;

    private final Channel channel;
    private final long invokeId;

    private byte sendState = SENDING;

    public RegistryRequestFuture(Channel channel, long invokeId) {
        this.channel = channel;
        this.invokeId = invokeId;
        FUTURE_HOLDER.put(invokeId, this);
    }

    public void sentSuccess() {
        sendState = SEND_SUCCESS;
    }

    public void sentFailure() {
        sendState = SEND_FAILURE;
    }

    @Override
    public RegistryResponse response() throws Exception {
        return get(1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onSentSuccess() {

    }

    @Override
    public void onSentFailure() {

    }

    public static void received(Channel ch, long xid, RegistryResponse response) {
        RegistryRequestFuture invokeFuture = FUTURE_HOLDER.remove(xid);
        if (invokeFuture == null) {
            return;
        }
        // 进行异步通知
        invokeFuture.complete(response);
    }

}
