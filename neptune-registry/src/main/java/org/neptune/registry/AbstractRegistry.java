package org.neptune.registry;

import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.neptune.common.util.ConcurrentSet;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRegistry implements Registry {

    private final ConcurrentHashMap<ServiceMeta, ServiceSubscriber.RegistryNotifier> subscribedServices = new ConcurrentHashMap<>();

    private final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));

    private final ConcurrentHashMap<ServiceMeta, ConcurrentSet<RegistryMeta>> SERVICE_PROVIDER_MAP = new ConcurrentHashMap<>(128);


    protected void updateServiceList(final ServiceMeta serviceMeta, List<RegistryMeta> serviceProviders) {
        synchronized (SERVICE_PROVIDER_MAP) {
            ConcurrentSet<RegistryMeta> registryMetas = SERVICE_PROVIDER_MAP.getOrDefault(serviceMeta, new ConcurrentSet<>());
            registryMetas.addAll(serviceProviders);
            SERVICE_PROVIDER_MAP.put(serviceMeta, registryMetas);
        }
    }

    abstract protected void actionAfterRegister();

    abstract protected void actionAfterSubscribe(ServiceMeta serviceMeta, ServiceSubscriber.RegistryNotifier notifier);

}
