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
package org.neptune.core.consumer.handler;

import org.neptune.core.*;
import org.neptune.core.consumer.Dispatcher;
import org.neptune.core.consumer.cluster.ClusterInvoker;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

/**
 * org.neptune.core.consumer - RpcInvoker
 * <p>
 * 考虑这里做拦截链, 比如日志打印, 统一结果
 *
 * @author tony-is-coding
 * @date 2021/12/17 17:46
 */
public abstract class AbstractInvocationHandler {

    protected ClusterInvoker clusterInvoker; // 集群方案应该是要默认支持的
    protected ServiceMeta serviceMeta;
    protected Client client;
    protected Dispatcher dispatcher;

    protected Object doInvoke(String methodName, Object[] args, Class<?> returnType, boolean sync) throws Throwable {
        Request request = createRequest(methodName, args);
        //执行上下文, 用来在多个执行流中传递
        InvokeFuture<?> resultFuture = clusterInvoker.invoke(dispatcher, request, returnType);
        if (sync) {
            return resultFuture.getResult();
        }
        return resultFuture;
    }

    private Request createRequest(String methodName, Object[] args) {
        RequestBody body = new RequestBody(serviceMeta);
        body.setMethodName(methodName);
        body.setArgs(args);
        body.setAppName(client.appName());
        Request request = new Request(10100000L); // TODO: distribute unique ID
        request.setBody(body);
        return request;
    }

    public abstract Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable;

}
