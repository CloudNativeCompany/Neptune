package org.neptune.registry;

import org.junit.jupiter.api.Test;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.registry.nacos.NacosServicePublisher;


class NacosServicePublisherTest {

    @Test
    public void testPublish() throws Throwable {
        NacosServicePublisher publisher = new NacosServicePublisher(
                "127.0.0.1", "8848"
        );

        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServerVersion("1.0.0");
        serviceMeta.setServerName("demo-service");
        serviceMeta.setGroup("default");

        UnresolvedAddress address = new UnresolvedSocketAddress("127.0.0.1",8001);
        RegistryMeta registryMeta = new RegistryMeta();

        registryMeta.setAddress(address);
        registryMeta.setServiceMeta(serviceMeta);

        publisher.register(registryMeta);

        while (true){
            Thread.sleep(1000);
        }
    }

}