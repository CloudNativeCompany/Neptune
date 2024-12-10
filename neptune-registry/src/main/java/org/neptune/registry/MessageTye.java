package org.neptune.registry;


public enum MessageTye {


    PublishRequest    ((byte) 0x01),
    PublishResponse   ((byte) 0x02),
    SubscribeRequest  ((byte) 0x03),
    SubscribeResponse ((byte) 0x04),
    fetchServiceInstance ((byte) 0x05),
    noticeServiceInstanceAdded ((byte) 0x06),
    ;
    private final byte code;

    MessageTye(byte code) {
        this.code = code;
    }
    public static MessageTye codeOf(byte code) {
        for (MessageTye value : MessageTye.values()) {
            if(code == value.code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown MessageTye code: " + code);
    }
    public byte getCode() {
        return code;
    }
}
