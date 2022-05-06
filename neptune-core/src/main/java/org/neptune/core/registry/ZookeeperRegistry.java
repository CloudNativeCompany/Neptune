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
package org.neptune.core.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.neptune.core.Directory;
import org.neptune.core.ServiceMeta;
import org.neptune.core.util.SysPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.neptune.core.util.Requires.*;

/**
 * org.neptune.core.registry.impl - ZookeeperRegistry
 *
 * @author tony-is-coding
 * @date 2021/12/20 14:35
 */
public abstract class ZookeeperRegistry extends AbstractRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class.getName());

    private static final String address = SysPropertyUtil.get("neptune.server.address", "127.0.0.1");
    private static final int SESSION_TIMEOUT_MS = SysPropertyUtil.getInt("neptune.", 60 * 1000);
    private static final int CONNECTION_TIMEOUT_MS = SysPropertyUtil.getInt("curator-default-connection-timeout", 15 * 1000);

    protected CuratorFramework configClient;


    protected final String getDirectory(ServiceMeta meta) {
        return String.format("/neptune/provider/%s/%s/%s",
                meta.getGroup(),
                meta.getServiceName(),
                meta.getVersion());
    }

    @Override
    public void connectToRegistryServer(String connectString) {
        requireNotNull(connectString, "registry connection string");

        configClient = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        configClient.getConnectionStateListenable().addListener((client, newState) -> {
            logger.info("Zookeeper connection state changed {}.", newState);
            if (newState == ConnectionState.RECONNECTED) {
                fireOnReconnectToZk(client, newState);
            }
        });
        configClient.start();
    }

    @Override
    public void shutdownGracefully() {

    }
    protected abstract void fireOnReconnectToZk(CuratorFramework client, ConnectionState newState);
}
