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
package org.neptune.core.consumer.lb;

import org.neptune.connect.ConnectionGroup;
import org.neptune.connect.ServiceConnectionHolder;

/**
 * org.neptune.core.core - LoadBalancer
 *
 * @author tony-is-coding
 * @date 2021/12/20 16:58
 */
public interface LoadBalancer {

    /*
        README: 考虑并发管理
     */

    ConnectionGroup select(ServiceConnectionHolder container);

    enum LoadBalancerType{
        RANDOM(1), // 随机法
        ROUND_ROBIN(2), // 轮询
        WEIGHT_RANDOM(3), // 加权随机
        WEIGHT_ROUND_ROBIN(4), // 加权轮询
        HASH(5), // 一致性hash, 源地址一致性 hash
        ;
        private final int code;

        LoadBalancerType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static LoadBalancerType getDefault(){
            return ROUND_ROBIN;
        }
    }
}
