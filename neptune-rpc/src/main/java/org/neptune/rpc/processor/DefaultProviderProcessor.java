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
package org.neptune.rpc.processor;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.neptune.rpc.RequestBody;
import org.neptune.rpc.ResponseBody;
import org.neptune.rpc.factories.SerializerFactory;
import org.neptune.rpc.seialize.Serializer;
import org.neptune.transport.RequestPayload;
import org.neptune.transport.ResponsePayload;
import org.neptune.transport.Status;
import org.neptune.transport.processor.ProviderProcessor;

/**
 * org.neptune.rpc.core - DefaultProviderProcessor
 *
 * @author tony-is-coding
 * @date 2021/12/24 16:07
 */
@Slf4j
public class DefaultProviderProcessor implements ProviderProcessor {

    @Override
    public void shutdownGracefully() {
    }

    @Override
    public void handleRequest(Channel channel, RequestPayload request) throws Exception {
        Serializer serializer = SerializerFactory.getSerializer(Serializer.SerializerType.parse(request.getSerialTypeCode()));
        // 直接pong 回去
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult("this is an result from remote sever!! good day");

        ResponsePayload payload = new ResponsePayload(request.getXid());
        payload.setStatus(Status.OK.value());
        payload.setSerialTypeCode(request.getSerialTypeCode());
        payload.setBytes(serializer.writeObject(responseBody));

        channel.writeAndFlush(payload).addListener(
                (ChannelFutureListener) cf -> {});
    }

    @Override
    public void handleException(Channel channel, RequestPayload request, Status status, Throwable cause) {
        log.info("错误发生");
    }
}
