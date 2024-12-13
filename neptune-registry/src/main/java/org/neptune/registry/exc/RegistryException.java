package org.neptune.registry.exc;

import org.neptune.registry.RegistryStatus;

public class RegistryException extends RuntimeException{

    private byte code;
    private String errMsg;

    public RegistryException(byte code, String errMsg) {
        super(errMsg);
        this.code = code;
        this.errMsg = errMsg;
    }

    public RegistryException(RegistryStatus status, String errMsg) {
        this(status.value(), errMsg);
    }

    public RegistryException(String errMsg) {
        this((byte) 0, errMsg);
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
