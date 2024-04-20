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

import org.neptune.registry.ServiceMeta;
import org.neptune.rpc.client.cluster.ClusterInvoker;
import org.neptune.rpc.client.cluster.ClusterInvokerFactory;
import org.neptune.rpc.client.handler.ByteBuddyInvocationHandlerBridge;
import org.neptune.common.util.Requires;
import org.neptune.common.util.ThrowUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


import static org.neptune.common.util.Requires.*;

/**
 * org.neptune.rpc.consumer - ProxyFactory
 *
 * @author tony-is-coding
 * @date 2021/12/17 17:34
 */

public class ProxyFactory<I> {

    // 接口类型
    private final Class<I> interfaceClass;

    private ServiceMeta serviceMeta;
    private Client client;
    private FactoryProxy factoryProxy;


    public static <I> ProxyFactory<I> factory(Class<I> interfaceClass) {
        return new ProxyFactory<>(interfaceClass);
    }

    public ProxyFactory<I> serviceMeta( ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
        return this;
    }

    public ProxyFactory<I> client(Client client) {
        this.client = client;
        return this;
    }

    public ProxyFactory<I> factoryProxy( FactoryProxy factoryProxy) {
        this.factoryProxy = factoryProxy;
        return this;
    }

    public I newInstance() {
        requireNotNull(interfaceClass, "proxy interface  must be assigned");

        Dispatcher dispatcher = new DefaultDispatcher(factoryProxy.getLoadBalancerType(), factoryProxy.getSerializerType(), client);
        ClusterInvoker clusterInvoker = ClusterInvokerFactory.create(factoryProxy.getClusterStrategy());
        Object handler = new ByteBuddyInvocationHandlerBridge(
                clusterInvoker,
                serviceMeta,
                client,
                dispatcher,factoryProxy.isAsyncInvoke()
        );
        return FactoryDelegate.BYTE_BUDDY.newProxy(interfaceClass, handler);
    }

    private ProxyFactory(Class<I> clz) {
        this.interfaceClass = clz;
    }

    private interface FactoryDelegate {
        // todo: 期望做到和 spring 一样支持动态得选择代理技术

        // todo: finish jdk proxy;
        FactoryDelegate JDK_PROXY = new FactoryDelegate() {
            @Override
            public <T> T newProxy(Class<T> interfaceType, Object handler) {
                Requires.requireTrue(handler instanceof InvocationHandler, "handler must be a InvocationHandler");
                Object object = Proxy.newProxyInstance(
                        interfaceType.getClassLoader(), new Class<?>[]{interfaceType}, (InvocationHandler) handler);
                return interfaceType.cast(object);
            }
        };

        FactoryDelegate BYTE_BUDDY = new FactoryDelegate() {
            @Override
            public <T> T newProxy(Class<T> interfaceType, Object handler) {
                Class<? extends T> cls = new ByteBuddy()
                        .subclass(interfaceType) //README: 创建的是一个继承自interfaceType的类(支持对class, 但是RPC还是抽象到接口高度比较好)
                        .method(ElementMatchers.isDeclaredBy(interfaceType)) //README: 初略代理所有接口定义的方法, 也可以自己去实现过滤
                        .intercept(MethodDelegation.to(handler)) // 将所有方法都代理给handler 的方法捕捉器
                        .make()
                        .load(interfaceType.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                        .getLoaded();

                try {
                    return cls.newInstance();
                } catch (Throwable t) {
                    ThrowUtil.throwException(t);
                }
                return null; // never reach here
            }
        };

        <T> T newProxy(Class<T> interfaceType, Object handler);
    }

}
