package org.neptune.registry;

import java.io.Serializable;

public class RegistryResponse implements Serializable {

    private byte code;

    private String msg;

    private Object body;

    public RegistryResponse() {
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
