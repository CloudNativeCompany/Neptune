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
package org.neptune.core.consumer.cluster;

import org.neptune.core.InvokeFuture;
import org.neptune.core.Request;
import org.neptune.core.consumer.Dispatcher;

/**
 * org.neptune.core.core - ClusterInvoker
 * 集群调度抽象接口, 提供集群调度的方案
 *
 * @author tony-is-coding
 * @date 2021/12/20 18:15
 */
public interface ClusterInvoker {

    <T> InvokeFuture<T> invoke(Dispatcher dispatcher, Request request, Class<T> returnType) throws Throwable;

    enum ClusterStrategy {
        FAIL_FAST,  // 快速失败
        FAIL_OVER,  // 失败重试
        FAIL_SAFE,
        ;

        public static ClusterStrategy getDefault(){
            return FAIL_FAST;
        }
    }

    ClusterStrategy strategy();
}
