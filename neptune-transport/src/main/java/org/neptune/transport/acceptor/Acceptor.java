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
package org.neptune.transport.acceptor;

import org.neptune.common.UnresolvedAddress;
import org.neptune.transport.processor.ProviderProcessor;

import java.net.SocketAddress;

/**
 * org.neptune.rpc.transportLayer - Acceptor
 * 监听器
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:22
 */
//
public interface Acceptor {

    UnresolvedAddress resolvedAddress();

    SocketAddress localAddress();

    void startAsync() throws Exception;

    void start(boolean sync) throws Exception;

    // void sync(); // 是否可以像netty一样?

    void shutdownGracefully();

    void withProcessor(ProviderProcessor processor);
}
