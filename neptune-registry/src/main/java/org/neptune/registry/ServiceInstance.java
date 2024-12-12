package org.neptune.registry;

import io.netty.channel.Channel;

import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return
                Objects.equals(address.port(), that.address.port()) &&
                        Objects.equals(address.host(), that.address.host()) &&
                        Objects.equals(serviceMeta.getServerName(), that.serviceMeta.getServerName()) &&
                        Objects.equals(serviceMeta.getGroup(), that.serviceMeta.getGroup()) &&
                        Objects.equals(serviceMeta.getServerVersion(), that.serviceMeta.getServerVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(address.port(), address.host(), serviceMeta.getServerName(),
                serviceMeta.getGroup(), serviceMeta.getServerVersion());
    }

}
