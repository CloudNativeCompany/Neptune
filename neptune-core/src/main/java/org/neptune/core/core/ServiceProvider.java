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
package org.neptune.core.core;

import org.neptune.core.core.registry.RegistryMeta;

import java.net.SocketAddress;
import java.util.concurrent.Executor;

/**
 * org.neptune.core.core.registry - ServiceProvider
 *
 * @author tony-is-coding
 * @date 2021/12/22 11:38
 */
public class ServiceProvider extends RegistryMeta {
    Object provider;
    Class<?> interfaceClass;
    Executor executor;

    public ServiceProvider serviceMeta(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
        return this;
    }

    public ServiceProvider provider(Object provider) {
        this.provider = provider;
        return this;
    }

    public ServiceProvider executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public ServiceProvider interfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        return this;
    }


}
