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

/**
 * org.neptune.core.core - ServicePublisher
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:07
 */
public interface ServicePublisher extends Registry {

    void register(RegistryMeta meta, RegisterListener listener);

    void unregister(RegistryMeta meta, RegisterListener listener);


    void register(RegistryMeta meta) throws Throwable;

    void unregister(RegistryMeta meta) throws Throwable;

    interface RegisterListener {
        void onCompleted();

        void onFailure();
    }
}
