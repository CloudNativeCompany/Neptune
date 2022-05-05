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
package org.neptune.core.transport;

import java.net.InetSocketAddress;

/**
 * org.neptune.core.transportLayer - Acceptor
 * 监听器
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:22
 */
//
public interface Acceptor {
    int boundPort();

    InetSocketAddress localAddress();

    void start() throws InterruptedException;

    void start(boolean sync) throws InterruptedException;

    // void sync(); // 是否可以像netty一样?

    void shutdownGracefully();
}
