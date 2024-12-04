package org.neptune.registry;

import java.io.Serializable;

public class PublishResponse implements Serializable {
    private static final long serialVersionUID = 1009813828866652852L;
    /**
     * @see RegistryStatus
     */
    private int code;

    private String msg;

    private Object data;

    public PublishResponse() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
