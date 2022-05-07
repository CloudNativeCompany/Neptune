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
package org.neptune.core;

import org.neptune.transport.Directory;

import java.io.Serializable;

/**
 * org.neptune.core.core - ServiceMeta
 * 为什么有这一层?, 目的是为了 服务注册/发现 过程中的的服务传递打包 目前可有可无..
 *
 * @author tony-is-coding
 * @date 2021/12/17 16:11
 */
public class ServiceMeta extends Directory implements Serializable {

    private static final long serialVersionUID = -8908295634641380163L;

    public ServiceMeta() {
    }

    public ServiceMeta(String group, String serviceName, String version) {
        super(group, serviceName, version);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
