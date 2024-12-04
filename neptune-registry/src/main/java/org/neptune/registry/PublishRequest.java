package org.neptune.registry;

import java.io.Serializable;

public class PublishRequest implements Serializable {
    private static final long serialVersionUID = 1009813828866652852L;

    private RegistryMeta registryMeta;

    public PublishRequest() {
    }

    public RegistryMeta getRegistryMeta() {
        return registryMeta;
    }

    public void setRegistryMeta(RegistryMeta registryMeta) {
        this.registryMeta = registryMeta;
    }
}
