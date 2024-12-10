package org.neptune.example.registry;

import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;
import org.neptune.registry.defaultimpl.DefaultPublisher;
import org.neptune.registry.defaultimpl.DefaultSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class TestDefaultPublisher {

    private static final Logger logger = LoggerFactory.getLogger(TestDefaultPublisher.class.getName());
    public static void main(String[] args) throws Exception {

        DefaultPublisher defaultPublisher = new DefaultPublisher("127.0.0.1", 8001);
        ServiceMeta serviceMeta = new ServiceMeta(
                "fgptas",
                "1.0.0",
                "fi"
        );
        RegistryMeta registryMeta = new RegistryMeta();
        registryMeta.setServiceMeta(serviceMeta);
        registryMeta.setWight(100);;
        registryMeta.setAddress(new UnresolvedSocketAddress("127.0.0.1", 8002));
        defaultPublisher.register(registryMeta);
    }

}
