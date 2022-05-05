/*
 * Copyright (c) 2015 The Neptune Project
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
package org.neptune.core.core.seialize;

import org.neptune.core.core.exec.DeserializerException;
import org.neptune.core.core.exec.SerializerException;

/**
 * org.neptune.core.core.seialize - Serializer
 *
 * @author tony-is-coding
 * @date 2021/12/24 14:38
 */
public interface Serializer {
    /*
        TODO: 目前还是个很简单的 Serializer; 需要后续进行优化, 目前有以下优化方向
            1. buffer复用, 可以优化具体序列化器底层的序列化buffer,前提只要保证不线程序列化隔离即可
            2. 数据从 业务线程 到 IO 线程可以利用上netty的 ByteBuf 优化减少拷贝开销
     */

    byte typeCode();

    /**
     * 序列化
     */
    byte[] writeObject(Object from) throws SerializerException;

    /**
     * 反序列化
     */
    <T> T readObject(byte[] to, int offset, int length, Class<T> clazz) throws DeserializerException;

    enum SerializerType {
        JAVA_NATIVE(1),
        KRYO(2),
        PROTO_STUFF(3),
        ;

        private final int code;

        SerializerType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public SerializerType parse(String name) {
            name = name.toLowerCase();
            switch (name) {
                case "kryo":
                    return KRYO;
                case "proto_stuff":
                    return PROTO_STUFF;
                case "java":
                default:
                    return JAVA_NATIVE;

            }
        }

        public static SerializerType getDefault(){
            return KRYO;
        }
    }

}


