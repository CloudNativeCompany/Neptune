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


import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.neptune.transport.RequestFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * org.neptune.rpc.core - DefaultInvokeFuture
 * 通过这个进行阶段通知
 *
 * @author tony-is-coding
 * @date 2021/12/20 17:56
 */
@Slf4j
public class DefaultRequestFuture<T> extends CompletableFuture<T> implements RequestFuture<T> {

    private static final ConcurrentHashMap<Long, DefaultRequestFuture<?>> FUTURE_HOLDER = new ConcurrentHashMap<>(16);
    private static final byte SEND_FAILURE = -1;
    private static final byte SEND_SUCCESS = 1;
    private static final byte SENDING = 0;

    private final Channel channel;
    private final long invokeId;
    private final Class<T> returnType;

    private byte sendState = SENDING;

    public DefaultRequestFuture(Channel channel, long invokeId,  Class<T> returnType) {
        this.channel = channel;
        this.invokeId = invokeId;
        this.returnType = returnType;
        FUTURE_HOLDER.put(invokeId, this);
    }

    @Override
    public T response() throws Exception {
        try{
            return get(2000, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            log.error("timeout__ invokeId:{}, channel:{}", invokeId, channel.toString());
            throw e;
        }
    }

    @Override
    public void onSentSuccess() {
        sendState = SEND_SUCCESS;
    }

    @Override
    public void onSentFailure() {
        sendState = SEND_FAILURE;
    }

    public static void received(Channel ch, long xid, RpcResponse rpcResponse) {
        DefaultRequestFuture<?> invokeFuture = FUTURE_HOLDER.remove(xid);
        if (invokeFuture == null) {
            return;
        }
        // 进行异步通知
        invokeFuture.doReceived(rpcResponse);
    }

    @SuppressWarnings("unchecked")
    private void doReceived(RpcResponse rpcResponse) {
        final Object result = rpcResponse.getResult();
        complete((T) result); // 完成
    }
}
