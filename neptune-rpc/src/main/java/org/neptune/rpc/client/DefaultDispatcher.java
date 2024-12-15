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
package org.neptune.rpc.client;

import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.util.IdGenerator;
import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;
import org.neptune.rpc.*;
import org.neptune.rpc.client.lb.LoadBalancer;
import org.neptune.rpc.client.lb.LoadBalancerFactory;
import org.neptune.transport.RequestFuture;
import org.neptune.transport.seialize.SerializerFactory;
import org.neptune.transport.seialize.Serializer;

import org.neptune.transport.RequestPayload;
import io.netty.channel.Channel;

import java.util.Set;

/**
 * org.neptune.rpc.consumer - DefaultDispatcher
 *
 * @author tony-is-coding
 * @date 2021/12/26 15:09
 */
@Slf4j
public class DefaultDispatcher implements Dispatcher {
    /*
        考虑共用性

        README: 在这一层完成
            1. 负载均衡
            2. 拦截器拓展
            3. 业务数据序列化 && send 数据
            4. 超时控制?
     */
    private final LoadBalancer loadBalancer;
    private final Serializer serializer;
    private final Client client;

    public DefaultDispatcher(LoadBalancer.LoadBalancerType loadBalancerType, Serializer.SerializerType serializerType, Client client) {
        this.loadBalancer = LoadBalancerFactory.create(loadBalancerType);
        this.serializer = SerializerFactory.getSerializer(serializerType);
        this.client = client;
    }

    @Override
    public <T> RequestFuture<T> dispatch(RpcRequest request, Class<T> returnType) {
        try {
            return send(request, returnType);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // 匹配一个目标连接来
    private Channel select(ServiceMeta serviceMeta) throws Throwable {
        //TODO: load balance 是基于registry 的结果做的
        //TODO: 这一层的抽象还是需要再看看
        Set<RegistryMeta> serviceInstance = client.serviceSubscriber().serviceList(serviceMeta);
        UnresolvedAddress address = loadBalancer.select(serviceInstance);
        return client.getConnector().getAddressConnects(address).next().channel();
    }


    private <T> RequestFuture<T> send(RpcRequest request, Class<T> returnType) throws Throwable {
        final long invokeId = IdGenerator.newId();
        // 对象序列化
        RequestPayload payload = new RequestPayload(invokeId);
        payload.setSerialTypeCode(serializer.typeCode());
        payload.setBytes(serializer.writeObject(request));
        Channel ch = select(request.getMetadata());
        RequestFuture<T> invokeFuture = new DefaultRequestFuture<>(ch, invokeId,returnType);
        ch.writeAndFlush(payload).addListener(
                // TODO:加入发送超时监控, writeAndFlush
                (ChannelFutureListener) cf -> {
                    if (cf.isSuccess()) { // success
                        invokeFuture.onSentSuccess();
                    } else { // fail
                        invokeFuture.onSentFailure();
                    }
                });
        return invokeFuture;
    }
}
