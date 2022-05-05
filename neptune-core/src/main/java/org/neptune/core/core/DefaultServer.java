/*
 * Copyright (c) 2015 The Neptune Project
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
package org.neptune.core.core;

import org.neptune.core.core.registry.ServicePublisher;
import org.neptune.core.transport.Acceptor;
import org.neptune.core.util.ExtensionLoader;


/**
 * org.neptune.core.core - DefaultServer
 *
 * @author tony-is-coding
 * @date 2021/12/22 11:19
 */
public class DefaultServer implements Server {

    Acceptor acceptor;
    ServicePublisher servicePublisher = null;
    boolean running = false;

    @Override
    public Acceptor acceptor() {
        return null;
    }


    @Override
    public ServicePublisher connectToRegistryServer(String address) {
        // load service publisher
        servicePublisher = ExtensionLoader.load(ServicePublisher.class).first();
        return servicePublisher;
    }

    @Override
    public void publish(ServiceProvider serviceProvider) {
    }

    @Override
    public void publish(ServiceProvider... serviceProviders) {

    }

    @Override
    public void cancelPublish(ServiceProvider serviceProvider) {

    }

    @Override
    public void cancelPublish(ServiceProvider... serviceProviders) {

    }

    @Override
    public void start() {

    }

    @Override
    public ServiceProvider serviceProvider() {
        ServiceProvider provider = new ServiceProvider();
        return provider;
    }

    @Override
    public void shutdownGracefully() {
        acceptor.shutdownGracefully();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    protected void cancelPublishAll() {
        // cancel all the published service
    }

    protected void doPublish(ServiceProvider serviceProvider) {
    }


}
