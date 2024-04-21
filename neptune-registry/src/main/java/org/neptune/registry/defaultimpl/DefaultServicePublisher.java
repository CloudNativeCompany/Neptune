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
package org.neptune.registry.defaultimpl;

import org.neptune.registry.AbstractServicePublisher;
import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;
import org.neptune.registry.ServiceSubscriber;


import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * org.neptune.rpc.core - DefaultServicePublisher
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:16
 */
public class DefaultServicePublisher extends AbstractServicePublisher {

    private final LinkedBlockingQueue<RegistryMeta> queue = new LinkedBlockingQueue<>();

    @Override
    public void register(RegistryMeta meta, RegisterListener listener) {

    }

    @Override
    public void unregister(RegistryMeta meta, RegisterListener listener) {

    }

    @Override
    public void register(RegistryMeta meta) throws Exception{

    }

    @Override
    public void unregister(RegistryMeta meta) throws Exception{

    }

    @Override
    public void shutdownGracefully() {

    }
}
