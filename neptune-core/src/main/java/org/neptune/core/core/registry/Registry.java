/*
 * Copyright (c) 2015 The Jupiter Project
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

import org.neptune.core.transport.UnresolvedAddress;

/**
 * org.neptune.core.core - Registry
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:07
 */
public interface Registry {
    void connectToRegistryServer(UnresolvedAddress connectString);

    void shutdownGracefully();

    enum RegistryType {
        DEFAULT("default"),
        // CONSUL("consul"),
        ZOOKEEPER("zookeeper");

        private final String value;
        RegistryType(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public static RegistryType parse(String name) {
            for (RegistryType s : values()) {
                if (s.name().equalsIgnoreCase(name)) {
                    return s;
                }
            }
            return null;
        }
    }
}
