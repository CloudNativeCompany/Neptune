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
package org.neptune.core.seialize;

import org.neptune.core.exec.DeserializerException;
import org.neptune.core.exec.SerializerException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * org.neptune.core.seialize - KryoSerializer
 *
 * @author tony-is-coding
 * @date 2021/12/24 14:40
 */
public class KryoSerializer extends AbstractSerializer {

    private static final ConcurrentHashMap.KeySetView<Class<?>, Boolean> useJavaSerializerTypes = ConcurrentHashMap.newKeySet();

    static {
        useJavaSerializerTypes.add(Throwable.class);
    }

    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {

        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            for (Class<?> type : useJavaSerializerTypes) {
                kryo.addDefaultSerializer(type, JavaSerializer.class);
            }
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);
            return kryo;
        }
    };


    @Override
    public byte typeCode() {
        return (byte) 0x00;
    }

    @Override
    public byte[] writeObject(Object obj) {
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Output output = new Output(outputStream)
        ) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj); // 将对象写入到包装的输出流
            return output.toBytes();

        } catch (IOException e) {
            throw new SerializerException("Failed to serialize a " + obj.getClass().toString() + " object; cause by: " + e.getMessage());
        }
    }

    @Override
    public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz) {
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                Input input = new Input(inputStream)
        ) {
            Kryo kryo = kryoThreadLocal.get();
            return kryo.readObject(input, clazz);
        } catch (IOException e) {
            throw new DeserializerException("Failed to deserialize a " + clazz.toString() + " object; cause by: " + e.getMessage());
        }

    }
}