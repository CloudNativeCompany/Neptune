package org.neptune.example.registry;

import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;
import org.neptune.registry.ServiceSubscriber;
import org.neptune.registry.defaultimpl.DefaultSubscriber;
import org.neptune.transport.seialize.KryoSerializer;
import org.neptune.transport.seialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestDefaultSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(TestDefaultSubscriber.class.getName());
    public static void main(String[] args) {
        DefaultSubscriber defaultSubscriber = new DefaultSubscriber("127.0.0.1", 8001);
        ServiceMeta serviceMeta = new ServiceMeta(
                "fgptas",
                "1.0.0",
                "fi"
        );
        defaultSubscriber.subscribe(serviceMeta, new ServiceSubscriber.RegistryNotifier() {
            @Override
            public void notify(RegistryMeta registryMeta, EventType eventType) {
                logger.info("收到服务端推送的通知{} : {}", registryMeta.toUniqueInstanceId(), eventType);
            }
        });

        defaultSubscriber.consumers();

    }
}
