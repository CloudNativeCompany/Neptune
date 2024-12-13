package org.neptune.registry;


import org.neptune.transport.Status;

/**
 * org.neptune.rpc.registry - RegistryMeta
 *
 * @author tony-is-coding
 * @date 2024/12/04 12:40
 */
public enum RegistryStatus implements Status {

    SUCCESS                  ((byte) 0x00, "SUCCESS"),
    INTERNAL_ERROR           ((byte) 0x10, "INTERNAL_ERROR"),
    SERVICE_NOT_FOUND        ((byte) 0x20, "SERVICE_NOT_FOUND"),
    PUBLISH_FAIL             ((byte) 0x30, "PUBLISH_FAIL"),


    ;      // 客户端反序列化错误
    RegistryStatus(byte value, String description) {
        this.value = value;
        this.description = description;
    }

    private final byte value;
    private final String description;

    @Override
    public byte value() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }
}
