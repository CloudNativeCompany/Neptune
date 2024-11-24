package org.neptune.registry.defaultimpl;

import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;
import org.neptune.registry.ServiceSubscriber;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DefaultSubscriber implements ServiceSubscriber {


    @Override
    public Map<Object, Integer> consumers() {
        return Collections.emptyMap();
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, RegistryNotifier notifier) {

    }

    @Override
    public void unsubscribe(ServiceMeta serviceMeta) {

    }

    @Override
    public Set<RegistryMeta> serviceList(ServiceMeta serviceMeta) {
        return Collections.emptySet();
    }

    @Override
    public void shutdownGracefully() {

    }
}
