package org.neptune.registry;

import java.io.Serializable;

public class SubscribeRequest implements Serializable {
    private static final long serialVersionUID = 1009813828866652852L;

    private ServiceMeta serviceMeta;

    public SubscribeRequest() {
    }

    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }
    public void setServiceMeta(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
    }
}
