package org.neptune.registry;

import io.netty.channel.Channel;

public class ServiceInstance extends InstanceMeta {
    Channel channel;
    ServiceMeta serviceMeta;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
    }
}
