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


import io.netty.channel.Channel;
import org.neptune.common.UnresolvedAddress;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * org.neptune.rpc.transportLayer - RpcChannelManager
 *  针对同一个rpc-服务地址的一组netty socket连接
 * @author tony-is-coding
 * @date 2021/12/25 16:32
 */
public class RpcChannelGroup {
    private UnresolvedAddress serviceAddr;
    private transient volatile CopyOnWriteArrayList<Channel> array; // 通过 volatile 来保障 读-写并发问题
    final transient ReentrantLock lock = new ReentrantLock();
}
