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
package org.neptune.connect;

import org.neptune.transport.Directory;
import org.neptune.transport.UnresolvedAddress;

import java.util.concurrent.ConcurrentHashMap;

/**
 * org.neptune.connect - ConnectionManager
 *
 * @author tony-is-coding
 * @date 2022/5/7 16:07
 */
public class ConnectionManager {
    /*
        README: 连接管理器
            1. 服务发布后: 往某一个实例下新增

     */
    private final ConcurrentHashMap<Directory, ServiceConnectionHolder> serviceConnectionHolders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UnresolvedAddress, ConnectionGroup> addressGroupMap = new ConcurrentHashMap<>();

    public ServiceConnectionHolder find(Directory directory) {
        return serviceConnectionHolders.get(directory);
    }

    public ConnectionGroup group(UnresolvedAddress address) {
        ConnectionGroup group = addressGroupMap.get(address);
        if(group == null){
            ConnectionGroup newGroup = new ConnectionGroup();
            group = addressGroupMap.putIfAbsent(address,newGroup); // 考虑到 new到put 非原子性问题, 这样做能少一次加锁
            if(group == null){
                group = newGroup;
            }
        }
        return group;
    }

}
