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
package org.neptune.transport;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * org.neptune.core.transportLayer - ConnectionGroup
 * 管理一个 remote address 的一组连接
 *
 * @author tony-is-coding
 * @date 2021/12/25 15:52
 */
public class ConnectionGroup implements ConnectionManager {
    SocketAddress remoteAddress;

    private volatile CopyOnWriteArrayList<Connection> array = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<String, Connection> connectionMap = new ConcurrentHashMap<>();


    public SocketAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public void cancelReconnectAll() {
        //README: 这个阶段应该再允许进行connect
    }

    @Override
    public void disconnectAll() {
    }

    @Override
    public Connection next() {
        return null;
    }

}

