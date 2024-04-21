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
public class DefaultProviderProcessor implements ProviderProcessor {

    @Override
    public void shutdownGracefully() {
    }

    @Override
    public void handleRequest(Channel channel, RequestPayload request) throws Exception {
        Serializer serializer = SerializerFactory.getSerializer(Serializer.SerializerType.parse(request.getSerialTypeCode()));
        RequestBody requestBody = serializer.readObject(request.getBytes(), 0 ,request.getBytes().length , RequestBody.class);
        System.out.println("进入到了业务端, 远程地址为:" + JSON.toJSONString(channel.remoteAddress()));
        System.out.println("进入到了业务端, 开始处理请求分发工作:" + JSON.toJSONString(requestBody));

        // 直接pong 回去
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult("this is an result from remote sever!! good day");

        ResponsePayload payload = new ResponsePayload(request.getXid());
        payload.setStatus(Status.OK.value());
        payload.setSerialTypeCode(request.getSerialTypeCode());
        payload.setBytes(serializer.writeObject(responseBody));

        System.out.println("业务处理完成, 开始写响应结果...");
        channel.writeAndFlush(payload).addListener(
                (ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) { // success
                        System.out.println("response success");
                    } else { // fail
                        System.out.println("response Failure");
                    }
                });
    }

    @Override
    public void handleException(Channel channel, RequestPayload request, Status status, Throwable cause) {
        System.out.println("错误发生");
    }
}
