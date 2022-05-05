/*
 * Copyright (c) 2015 The Jupiter Project
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
package org.neptune.core.transport;

/**
 * org.neptune.core.core - PayloadHolder
 *
 * @author tony-is-coding
 * @date 2021/12/24 13:53
 */
public class PayloadHolder {

    private byte serialTypeCode;

    private byte[] bytes;  // 实际 请求/响应体 字节数组

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public void setSerialTypeCode(byte serialTypeCode) {
        this.serialTypeCode = serialTypeCode;
    }

    public byte getSerialTypeCode() {
        return serialTypeCode;
    }
}
