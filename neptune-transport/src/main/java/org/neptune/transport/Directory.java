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


import org.neptune.common.util.Requires;

/**
 * org.neptune.rpc.registry - Directory
 * 服务查找目录
 *
 * @author tony-is-coding
 * @date 2021/12/17 16:11
 */
public class Directory {
    private transient String directoryCache;
    private final transient ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<>(); //使用netty fast thread local 代替？

    protected String appId;
    private String serviceName;
    protected String version;

    public Directory() {
    }

    public Directory(String serviceName, String version) {
        this.appId = Requires.requireNotNull(serviceName, "appId");
        this.version = Requires.requireNotNull(version, "version");
    }

    /**
     * 服务查找key生成算法[组名称-服务名称-版本号]
     *
     * @return service directory
     */
    public String directoryString() {
        if (directoryCache != null) {
            return directoryCache;
        }
        StringBuilder sb = stringBuilder.get();
        if (sb == null) {
            sb = new StringBuilder(32);
            stringBuilder.set(sb);
        } else {
            sb.setLength(0);
        }
        sb.append(appId).append('-')
                .append(version);
        directoryCache = sb.toString();
        return directoryCache;
    }

    // hashcode 和 equals 目的是为了本地服务字典映射
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Directory metadata = (Directory) o;
        return appId.equals(metadata.appId)
                && version.equals(metadata.version);
    }

    @Override
    public int hashCode() {
        int result = appId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
