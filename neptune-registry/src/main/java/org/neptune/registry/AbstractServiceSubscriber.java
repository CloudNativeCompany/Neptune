package org.neptune.registry;

import org.neptune.common.util.ConcurrentSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc TODO
 *
 * @author tony
 * @createDate 2024/4/19 11:35 上午
 */
public abstract class AbstractServiceSubscriber implements ServiceSubscriber{

    private final ConcurrentHashMap<ServiceMeta, ConcurrentSet<RegistryMeta>> SERVICE_PROVIDER_MAP = new ConcurrentHashMap<>(128);

    @Override
    public Map<Object, Integer> consumers() {
        return null;
    }

    protected void updateServiceList(final ServiceMeta serviceMeta, List<RegistryMeta> serviceProviders){
        synchronized (SERVICE_PROVIDER_MAP){
            ConcurrentSet<RegistryMeta> registryMetas = SERVICE_PROVIDER_MAP.getOrDefault(serviceMeta, new ConcurrentSet<>());
            registryMetas.addAll(serviceProviders);
            SERVICE_PROVIDER_MAP.put(serviceMeta, registryMetas);
        }
    }

    @Override
    public Set<RegistryMeta> serviceList(ServiceMeta serviceMeta) {
        return SERVICE_PROVIDER_MAP.get(serviceMeta);
    }
}
