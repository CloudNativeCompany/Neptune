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
package org.neptune.transport.connector;


import org.neptune.transport.connection.Connection;
import org.neptune.common.UnresolvedAddress;
import org.neptune.transport.connection.ConnectionGroup;
import org.neptune.transport.processor.ConsumerProcessor;

/**
 * org.neptune.rpc.transportLayer - Connector
 * 连接器
 *
 * @author tony-is-coding
 * @date 2021/12/16 0:21
 */
public interface Connector {

    Connection connect(UnresolvedAddress remoteSocketAddress, boolean async);

    ConsumerProcessor process();

    void shutdownGracefully();

    /**
     * no return null
     * @param address
     * @return
     */
    ConnectionGroup getAddressConnects(UnresolvedAddress address);

    void removeAddressConnects(UnresolvedAddress address);
}
