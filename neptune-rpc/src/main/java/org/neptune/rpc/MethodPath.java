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
package org.neptune.rpc;


/**
 *  方法查找路径, 主要方便快速查找
 *
 */
public abstract class MethodPath {

    private transient String pathCache;

    /** 服务所属组别 */
    public abstract String getGroup();

    /** 服务名称 */
    public abstract String getServiceProviderName();

    /** 服务版本号 */
    public abstract String getVersion();

    public String directoryString() {
        if (pathCache != null) {
            return pathCache;
        }

        StringBuilder buf = new StringBuilder();
        buf.append(getGroup())
                .append('-')
                .append(getServiceProviderName())
                .append('-')
                .append(getVersion());

        pathCache = buf.toString();

        return pathCache;
    }

    public void clear() {
        pathCache = null;
    }
}
