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
package org.neptune.rpc.client.handler;

import org.neptune.registry.ServiceMeta;
import org.neptune.rpc.client.Client;
import org.neptune.rpc.client.Dispatcher;
import org.neptune.rpc.client.cluster.ClusterInvoker;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

/**
 * org.neptune.rpc.consumer - AsyncInvocationHandler
 *
 * @author tony-is-coding
 * @date 2021/12/20 18:01
 */
public class ByteBuddyInvocationHandlerBridge extends AbstractInvocationHandler {

    public ByteBuddyInvocationHandlerBridge(ClusterInvoker clusterInvoker,
                                            ServiceMeta meta,
                                            Client client,
                                            Dispatcher dispatcher, boolean invokeAsync) {
        this.clusterInvoker = clusterInvoker;
        this.serviceMeta = meta;
        this.client = client;
        this.dispatcher = dispatcher;
        this.invokeAsync = invokeAsync;
    }

    @RuntimeType
    public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable {
        return doInvoke(method.getName(), args, method.getReturnType());
    }
}
