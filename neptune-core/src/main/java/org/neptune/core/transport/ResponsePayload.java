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
 * org.neptune.core.transportLayer - ResponsePayload
 *
 * @author tony-is-coding
 * @date 2021/12/24 14:04
 */
public class ResponsePayload extends PayloadHolder {
    private final long xid;
    private byte status;

    public ResponsePayload(long xid) {
        this.xid = xid;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getXid() {
        return xid;
    }
}
