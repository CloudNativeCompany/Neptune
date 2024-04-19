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
package org.neptune.rpc;

/**
 * org.neptune.rpc.core - Request
 *
 * @author tony-is-coding
 * @date 2021/12/17 18:15
 */
public class Request {

    private final long invokeId;            //  request-response 关联的唯一事务ID;

    private RequestBody body;               // 请求体,这部分数据需要序列化成协议体打包传输到 server 对端

    public Request(long invokeId) {
        this.invokeId = invokeId;
    }

    public long getInvokeId() {
        return invokeId;
    }

    public RequestBody getBody() {
        return body;
    }

    public void setBody(RequestBody body) {
        this.body = body;
    }
}

