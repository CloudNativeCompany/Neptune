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
package org.neptune.common;

/**
 * org.neptune.rpc.transportLayer - UnresolvedSocketAddress
 *
 * @author tony-is-coding
 * @date 2021/12/25 17:07
 */
public final class UnresolvedSocketAddress implements UnresolvedAddress {

    private final int port;
    private final String host;

    public UnresolvedSocketAddress(String host,int port) {
        if (host == null) {
            throw new NullPointerException("host mustn't be null !!!");
        }
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("invalid socket port !!!");
        }
        this.port = port;
        this.host = host;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnresolvedSocketAddress that = (UnresolvedSocketAddress) o;

        return port == that.port && host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return host + ':' + port;
    }
}
