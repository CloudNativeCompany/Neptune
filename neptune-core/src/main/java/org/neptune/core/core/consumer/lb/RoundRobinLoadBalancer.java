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
package org.neptune.core.core.consumer.lb;

import org.neptune.core.transport.ConnectionGroup;
import org.neptune.core.transport.CowConnectionGroupList;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * org.neptune.core.core.consumer.lb - RoundRobinLoadBalancer
 *
 * @author tony-is-coding
 * @date 2021/12/27 17:04
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    private static final AtomicIntegerFieldUpdater<RoundRobinLoadBalancer> UPDATER = AtomicIntegerFieldUpdater.newUpdater(RoundRobinLoadBalancer.class, "index");


    private volatile int index = 0;

    @Override
    public ConnectionGroup select(CowConnectionGroupList container) {
        ConnectionGroup[] snapshot = container.snapshot();
        final int length = snapshot.length;
        if (length == 0) {
            return null;
        }
        if (length == 1) {
            return snapshot[0];
        }
        int nextIndex = UPDATER.getAndIncrement(this);
        // 不如何完美的轮询机制, 实际的轮询需要考虑
        return snapshot[nextIndex % length];
    }
}
