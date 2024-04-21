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
package org.neptune.rpc.server;

import lombok.extern.slf4j.Slf4j;
import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;
import org.neptune.registry.ServicePublisher;
import org.neptune.rpc.processor.DefaultProviderProcessor;
import org.neptune.transport.acceptor.Acceptor;
import org.neptune.transport.acceptor.NettyAcceptor;


/**
 * org.neptune.rpc.core - DefaultServer
 *
 * @author tony-is-coding
 * @date 2021/12/22 11:19
 */
@Slf4j
public class DefaultServer implements Server {

    private Acceptor acceptor;
    private ServicePublisher servicePublisher = null;
    int port;
    private boolean running = false;

    private String group;
    private String serverName;
    private String serverVersion;

    @Override
    public Acceptor acceptor() {
        return acceptor;
    }

    private DefaultServer(){}

    @Override
    public void start() throws InterruptedException {
        try{
            running = true;
            doPublishServer();
            acceptor.startAsync();
        }catch (Exception e){
            shutdownGracefully();
        }
    }


    private void doPublishServer(){
        RegistryMeta registryMeta = new RegistryMeta();
        ServiceMeta serviceMeta = new ServiceMeta();

        serviceMeta.setServerName(serverName);
        serviceMeta.setServerVersion(serverVersion);
        serviceMeta.setGroup(group);

        registryMeta.setWight(100);
        registryMeta.setServiceMeta(serviceMeta);
        registryMeta.setAddress(acceptor.resolvedAddress());

        try{
            servicePublisher.register(registryMeta);
        }catch (Exception e){
            throw new RuntimeException("registry server error");
        }

    }

    @Override
    public void shutdownGracefully() {
        running = false;
        acceptor.shutdownGracefully();
        servicePublisher.shutdownGracefully();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void addProvider(Class<?> providerClass) {

    }

    @Override
    public void addProviders(Class<?>... providerClass) {

    }

    public static DefaultServiceBuilder builder(){
        return new DefaultServiceBuilder();
    }


    public static class DefaultServiceBuilder {
        private final DefaultServer innerServer;

        public DefaultServiceBuilder() {
            innerServer = new DefaultServer();
            // default config
            innerServer.serverName = "default-server";
            innerServer.port = 8001;
        }

        public DefaultServer.DefaultServiceBuilder serverName(String serverName) {
            innerServer.serverName = serverName;
            return this;
        }

        public DefaultServer.DefaultServiceBuilder servicePublisher(ServicePublisher servicePublisher) {
            innerServer.servicePublisher = servicePublisher;
            return this;
        }

        public DefaultServer.DefaultServiceBuilder port(int port) {
            innerServer.port = port;
            return this;
        }

        public DefaultServer.DefaultServiceBuilder version(String version) {
            innerServer.serverVersion = version;
            return this;
        }

        public DefaultServer.DefaultServiceBuilder group(String group) {
            innerServer.group = group;
            return this;
        }


        public DefaultServer build() {
            innerServer.acceptor = new NettyAcceptor(innerServer.port);
            innerServer.acceptor.withProcessor(new DefaultProviderProcessor());
            return innerServer;
        }
    }


}
