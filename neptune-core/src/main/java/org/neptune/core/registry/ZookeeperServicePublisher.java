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
import org.apache.curator.framework.state.ConnectionState;
import org.neptune.core.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * org.neptune.core.registry.impl - ZookeeperServicePublisher
 *
 * @author tony-is-coding
 * @date 2021/12/20 14:36
 */
public class ZookeeperServicePublisher extends ZookeeperRegistry{

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServicePublisher.class.getName());

    @Override
    public void register(RegistryMeta meta, RegisterListener listener) {

    }

    @Override
    public void unregister(RegistryMeta meta, RegisterListener listener) {

    }

    @Override
    public void register(RegistryMeta meta) throws Throwable {

    }

    @Override
    public void unregister(RegistryMeta meta) throws Throwable {

    }

    @Override
    protected void fireOnReconnectToZk(CuratorFramework client, ConnectionState newState) {

    }


    private void doRegister(final RegistryMeta meta) {
        final ServiceMeta serviceMeta = meta.getServiceMeta();
        final String directory = getDirectory(serviceMeta);

        try {
            if (configClient.checkExists().forPath(directory) == null) {
                configClient.create().creatingParentsIfNeeded().forPath(directory);
            }
        } catch (Exception e) {
            logger.warn("Create parent path failed, directory: {}, {}.", directory, e);
        }

//        try {
//            meta.setHost(address);
//
//            // The znode will be deleted upon the client's disconnect.
//            configClient.create().withMode(CreateMode.EPHEMERAL).inBackground((client, event) -> {
//                if (event.getResultCode() == Code.OK.intValue()) {
//                    getRegisterMetaMap().put(meta, RegisterState.DONE);
//                }
//
//                logger.info("Register: {} - {}.", meta, event);
//            }).forPath(
//                    String.format("%s/%s:%s:%s:%s",
//                            directory,
//                            meta.getHost(),
//                            String.valueOf(meta.getPort()),
//                            String.valueOf(meta.getWeight()),
//                            String.valueOf(meta.getConnCount())));
//        } catch (Exception e) {
//            if (logger.isWarnEnabled()) {
//                logger.warn("Create register meta: {} path failed, {}.", meta, stackTrace(e));
//            }
//        }
    }

}
