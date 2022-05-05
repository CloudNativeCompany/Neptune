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

import org.neptune.transport.ConnectionGroup;
import org.neptune.transport.CowConnectionGroupList;

import java.util.Random;

/**
 * org.neptune.core.consumer.lb - RandomLoadBalancer
 *
 * @author tony-is-coding
 * @date 2021/12/27 16:19
 */
public class RandomLoadBalancer implements LoadBalancer {
    private static final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);

    @Override
    public ConnectionGroup select(CowConnectionGroupList container) {
        ConnectionGroup[] snapshot = container.snapshot();
        return snapshot[random.get().nextInt(snapshot.length)];
    }

}
