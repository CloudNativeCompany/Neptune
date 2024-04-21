/*
 * Copyright (c) 2022 The Neptune Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neptune.transport.protocol;

import java.io.Serializable;

/**
 * org.neptune.rpc.transportLayer - ProtocolHeader
 * 一个简单的RPC协议设计
 * <p>
 * **************************************************************************************************
 * Protocol
 * ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 * 2   │   1   │    1   │     8     │      4      │
 * ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 * │       │        │           │             │
 * │  MAGIC   Sign    Status   Invoke Id    Body Size                    Body Content              │
 * │       │        │           │             │
 * └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 * <p>
 * 消息头16个字节定长
 * = 2 // magic = (short) 0xabcd
 * + 1 // 消息标志位, 低地址4位用来表示消息类型request/response/heartbeat等, 高地址4位用来表示序列化类型
 * + 1 // 状态位, 设置请求响应状态
 * + 8 // 消息 id, long 类型, 未来考虑可能将id限制在48位, 留出高地址的16位作为扩展字段
 * + 4 // 消息体 body 长度, int 类型
 *
 * @author tony-is-coding
 * @date 2021/12/22 17:14
 */
public class ProtocolHeader implements Serializable {

    public static final int HEADER_SIZE = 1 << 4;

    // 消息类型常量
    public static final byte REQUEST                    = 0x01;     // Request
    public static final byte RESPONSE                   = 0x02;     // Response
    public static final byte HEARTBEAT                  = 0x03;     // Heartbeat

    /**
     * 2 byte 的校验头
     */
    public static final short MAGIC_WORD = (short) 0xabcd;

    /**
     * 序列化器吗 只有四位 0 - 15 最多支持16种
     */
    private byte serialTypeCode;

    /**
     * 消息类型 4 位  支持 16 种
     */
    private byte msgType;

    /**
     * 消息状态
     */
    private byte status;
    /**
     * 执行ID 主要是完成一个 请求-响应的串联
     */
    private long invokeId;

    /**
     * 消息体 限制一次传输最多有 5M 的数据
     */
    public static final int MAX_BODY_SIZE = 5 << 10 << 10;
    private int bodySize;


    public static boolean checkMagic(short magic){
        // 如果检查失败, 应该拒绝;
        return magic == MAGIC_WORD;
    }

    public void sign(byte sign) {
        this.serialTypeCode = (byte) (((int) sign & 0xff) >> 4);
        this.msgType = (byte) (sign & 0x0f);
    }
    public static byte toSign(byte serialTypeCode, byte msgType) {
        return (byte) (serialTypeCode << 4 | (0x0f & msgType));
    }


    public byte getSerialTypeCode() {
        return serialTypeCode;
    }

    public byte getMsgType() {
        return msgType;
    }

    public int getBodySize() {
        return bodySize;
    }

    public void status(byte status) {
        this.status = status;
    }

    public byte getStatus() {
        return status;
    }

    public void invokeId(long invokeId) {
        this.invokeId = invokeId;
    }

    public long getInvokeId() {
        return invokeId;
    }

    public void bodySize(int bodySize) {
        this.bodySize = bodySize;
    }

}
