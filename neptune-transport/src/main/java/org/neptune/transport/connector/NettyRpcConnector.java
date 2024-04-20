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

import io.netty.channel.ChannelOutboundHandler;
import org.neptune.transport.Connection;
import org.neptune.common.UnresolvedAddress;
import org.neptune.transport.processor.ConsumerProcessor;
import org.neptune.transport.protocol.ProtocolEncoder;

/**
 * org.neptune.rpc.transportLayer - NettyRpcConnector
 *
 * @author tony-is-coding
 * @date 2021/12/24 15:51
 */
public abstract class NettyRpcConnector implements Connector {
    /*
        说明: 拓展, 委托模式,(这个可以后期考虑)
            抽象层一: 支持多协议类型
                1. NettyTCP
                2. NettyDomain
                3. ...
            抽象层二: 支持多场景连接器
                1. RPC-Client Connector
                2. Registry-Client Connector
                3. ...
     */
    NettyConnector delegate;

    private ChannelOutboundHandler encoder = new ProtocolEncoder();

    @Override
    public Connection connect(UnresolvedAddress remoteSocketAddress, boolean async) {
        return delegate.connect(remoteSocketAddress, false);
    }


    @Override
    public ConsumerProcessor process() {
        return null;
    }

    @Override
    public void shutdownGracefully() {
    }
}
