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

import java.io.Serializable;

/**
 * org.neptune.rpc.core - ServiceMeta
 * 为什么有这一层?, 目的是为了 服务注册/发现 过程中的的服务传递打包 目前可有可无..
 *
 * @author tony-is-coding
 * @date 2021/12/17 16:11
 */
public class ServiceMeta implements Serializable {

    protected String appName;
    private String instanceId;
    protected String version;

    private static final long serialVersionUID = -8908295634641380163L;

    public ServiceMeta() {
    }

    public ServiceMeta(String appName, String instanceId, String version) {
        this.appName = appName;
        this.instanceId = instanceId;
        this.version = version;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
