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
 * org.neptune.rpc.core - Response
 *
 * @author tony-is-coding
 * @date 2021/12/17 18:15
 */
public class Response {

    long invokeId;     //  request-response 关联的唯一事务ID;

    ResponseBody body; //  这部分数据需要支持被序列化, 已支持传递到client对端

    public Object getResult() {
        return body.getResult();
    }

    public long getInvokeId() {
        return invokeId;
    }
}
