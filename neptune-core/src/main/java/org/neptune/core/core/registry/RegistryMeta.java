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
package org.neptune.core.core.registry;

import org.neptune.core.core.ServiceMeta;
import org.neptune.core.transport.UnresolvedAddress;

import java.net.InetSocketAddress;

/**
 * org.neptune.core.core.registry - RegistryMeta
 *
 * @author tony-is-coding
 * @date 2021/12/22 12:40
 */
public class RegistryMeta {
    protected ServiceMeta serviceMeta;
    protected UnresolvedAddress address;

    protected RegistryMeta() {
    }

    public void setAddress(UnresolvedAddress address) {
        this.address = address;
    }

    public UnresolvedAddress getAddress() {
        return address;
    }
}
