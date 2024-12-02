package org.neptune.registry;

import java.io.Serializable;

public class SubscribeResponse implements Serializable {
    private static final long serialVersionUID = 1009813828866652852L;

    private int code;

    public SubscribeResponse() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
