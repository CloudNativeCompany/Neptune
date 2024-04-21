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

import org.neptune.registry.ServiceMeta;

import java.io.Serializable;

/**
 * org.neptune.rpc.core - RequestBody
 *
 * @author tony-is-coding
 * @date 2021/12/24 14:19
 */
public class RequestBody implements Serializable {

    private static final long serialVersionUID = 1009813828866652852L;

    private String appName;                 // 当前应用 - 应用名称
    private final ServiceMeta metadata;     // 目标服务元数据
    private String methodName;              // 目标方法名称
    private Object[] args;                  // 目标方法参数

    public RequestBody(ServiceMeta metadata) {
        this.metadata = metadata;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ServiceMeta getMetadata() {
        return metadata;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
