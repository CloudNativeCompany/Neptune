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
package org.neptune.transport;

/**
 * org.neptune.rpc.transport - Status
 *
 * @author tony-is-coding
 * @date 2021/12/28 18:11
 */
public enum TransportStatus implements Status{

    DEFAULT                     ((byte) 0x00, "DEFAULT"),                   // 默认 - 无意义
    OK                          ((byte) 0x01, "OK"),                        // 正常 - 请求已完成
    CLIENT_ERROR                ((byte) 0x02, "CLIENT_ERROR"),              // 内部错误 — 因为意外情况, 客户端不能发送请求
    CLIENT_TIMEOUT              ((byte) 0x03, "CLIENT_TIMEOUT"),            // 超时 - 客户端超时
    SERVER_TIMEOUT              ((byte) 0x04, "SERVER_TIMEOUT"),            // 超时 - 服务端超时
    BAD_REQUEST                 ((byte) 0x05, "BAD_REQUEST"),               // 错误请求 — 请求中有语法问题, 或不能满足请求

    // APP 异常
    SERVICE_NOT_FOUND           ((byte) 0x06, "SERVICE_NOT_FOUND"),         // 找不到 - 指定服务不存在
    SERVER_ERROR                ((byte) 0x07, "SERVER_ERROR"),              // 内部错误 — 因为意外情况, 服务器不能完成请求
    SERVER_BUSY                 ((byte) 0x08, "SERVER_BUSY"),               // 内部错误 — 服务器太忙, 无法处理新的请求
    SERVICE_EXPECTED_ERROR      ((byte) 0x09, "SERVICE_EXPECTED_ERROR"),    // 服务错误 - 服务执行时出现预期内的异常
    SERVICE_UNEXPECTED_ERROR    ((byte) 0x10, "SERVICE_UNEXPECTED_ERROR"),  // 服务错误 - 服务执行意外出错
    APP_FLOW_CONTROL            ((byte) 0x11, "APP_FLOW_CONTROL"),          // 服务错误 - App级别服务限流
    DESERIALIZATION_FAIL        ((byte) 0x12, "DESERIALIZATION_FAIL");      // 客户端反序列化错误

    TransportStatus(byte value, String description) {
        this.value = value;
        this.description = description;
    }

    private final byte value;
    private final String description;

    public byte value() {
        return value;
    }

    public String description() {
        return description;
    }

}

