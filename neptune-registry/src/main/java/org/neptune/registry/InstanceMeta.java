package org.neptune.registry;

import org.neptune.common.UnresolvedAddress;

import java.io.Serializable;
import java.util.Objects;

public class InstanceMeta implements Serializable {

    private static final long serialVersionUID = -8908295634641380163L;

    protected UnresolvedAddress address;
    private int wight;

    public InstanceMeta() {
    }

    public UnresolvedAddress getAddress() {
        return address;
    }

    public void setAddress(UnresolvedAddress address) {
        this.address = address;
    }

    public int getWight() {
        return wight;
    }

    public void setWight(int wight) {
        this.wight = wight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceMeta that = (InstanceMeta) o;
        return
                Objects.equals(address.port(), that.address.port()) &&
                        Objects.equals(address.host(), that.address.host());
    }

    @Override
    public int hashCode() {
        return Objects.hash(address.port(), address.host());
    }
}
