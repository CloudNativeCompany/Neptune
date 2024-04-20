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
package org.neptune.registry;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * org.neptune.rpc.core - ServiceMeta
 * 为什么有这一层?, 目的是为了 服务注册/发现 过程中的的服务传递打包 目前可有可无..
 *
 * @author tony-is-coding
 * @date 2021/12/17 16:11
 */
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class ServiceMeta implements Serializable {

    private static final long serialVersionUID = -8908295634641380163L;

    protected String group;     // 这个设计是为了 环境隔离
    protected String appName;   // 应用名称 appid之类的东西
    protected String appVersion;   // 服务版本

    public ServiceMeta() {
    }

    public ServiceMeta(String appName, String appVersion, String group) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.group = group;
    }
}
