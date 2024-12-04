package org.neptune.registry;

import java.io.Serializable;

public class RegistryRequest implements Serializable {
    private byte   type;

    private Object body;

    public RegistryRequest() {
    }
    public RegistryRequest(byte type, Object body) {
        this.type = type;
        this.body = body;
    }
    public byte getType() {
        return type;
    }
    public void setType(byte type) {
        this.type = type;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
