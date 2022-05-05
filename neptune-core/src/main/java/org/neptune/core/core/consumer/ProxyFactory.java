/*
 * Copyright (c) 2015 The Neptune Project
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
package org.neptune.core.core.consumer;

import org.neptune.core.core.Client;
import org.neptune.core.core.ServiceMeta;
import org.neptune.core.core.annotation.RpcService;
import org.neptune.core.core.consumer.cluster.ClusterInvoker;
import org.neptune.core.core.consumer.cluster.ClusterInvokerFactory;
import org.neptune.core.core.consumer.handler.AsyncInvocationHandler;
import org.neptune.core.core.consumer.handler.SyncInvocationHandler;
import org.neptune.core.core.consumer.lb.LoadBalancer;
import org.neptune.core.core.seialize.Serializer;
import org.neptune.core.util.Requires;
import org.neptune.core.util.Strings;
import org.neptune.core.util.ThrowUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


import static org.neptune.core.util.Requires.*;

/**
 * org.neptune.core.core.consumer - ProxyFactory
 *
 * @author tony-is-coding
 * @date 2021/12/17 17:34
 */

public class ProxyFactory<I> {

    private static final String DEFAULT_VERSION = "1.0.0";

    // 接口类型
    private final Class<I> interfaceClass;

    private String group;
    private String serviceName;
    private String version;

    private Client client;
    private boolean asyncInvoke;

    private Serializer.SerializerType serializerType = Serializer.SerializerType.getDefault();
    private LoadBalancer.LoadBalancerType loadBalancerType = LoadBalancer.LoadBalancerType.getDefault();
    private ClusterInvoker.ClusterStrategy clusterStrategy = ClusterInvoker.ClusterStrategy.getDefault();

    public static <I> ProxyFactory<I> factory(Class<I> interfaceClass) {
        return new ProxyFactory<>(interfaceClass);
    }

    public ProxyFactory<I> group(String group) {
        this.group = group;
        return this;
    }

    public ProxyFactory<I> serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ProxyFactory<I> version(String version) {
        this.version = version;
        return this;
    }

    public ProxyFactory<I> client(Client client) {
        this.client = client;
        return this;
    }

    public ProxyFactory<I> invokeAsync() {
        this.asyncInvoke = true;
        return this;
    }

    public ProxyFactory<I> clusterStrategy(ClusterInvoker.ClusterStrategy clusterStrategy) {
        this.clusterStrategy = clusterStrategy;
        return this;
    }


    public I newInstance() {
        requireNotNull(interfaceClass, "proxy interface  must be assigned");
        if(serviceName != null){
            requireTrue(Strings.isNotBlank(serviceName), "serviceName");
        }

        RpcService annotation = interfaceClass.getAnnotation(RpcService.class);
        if (annotation != null) {
            // 显示配置优先级高
            if (group == null) {
                group = annotation.group();
            }
            if (serviceName == null) {
                serviceName = annotation.name();
            }
            if(Strings.isBlank(serviceName)){
                String[] split = interfaceClass.getName().split("\\.");
                serviceName = split[split.length - 1];
            }
        }
        requireTrue(Strings.isNotBlank(group), "group");

        requireNotNull(client, "client");

        if (Strings.isBlank(version)) {
            version = DEFAULT_VERSION;
        }

        ServiceMeta serviceMeta = new ServiceMeta(group, serviceName, version);
        Dispatcher dispatcher = new DefaultDispatcher(loadBalancerType, serializerType, client);
        ClusterInvoker clusterInvoker = ClusterInvokerFactory.create(clusterStrategy);

        Object handler;
        if (asyncInvoke) {
            handler = new AsyncInvocationHandler(
                    clusterInvoker,
                    serviceMeta,
                    client,
                    dispatcher
            );
        } else {
            handler = new SyncInvocationHandler(
                    clusterInvoker,
                    serviceMeta,
                    client,
                    dispatcher
            );
        }

        return FactoryDelegate.BYTE_BUDDY.newProxy(interfaceClass, handler);
    }

    private ProxyFactory(Class<I> clz) {
        this.interfaceClass = clz;
        asyncInvoke = false;
    }

    private interface FactoryDelegate {
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
