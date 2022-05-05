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
package org.neptune.core.util.internal;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author tony-is-coding
 * @date 2021/12/17 16:15
 */
@SuppressWarnings("unchecked")
final class UnsafeReferenceFieldUpdater<U, W> implements ReferenceFieldUpdater<U, W> {

    private final long offset;
    private final Unsafe unsafe;

    UnsafeReferenceFieldUpdater(Unsafe unsafe, Class<? super U> tClass, String fieldName) throws NoSuchFieldException {
        final Field field = tClass.getDeclaredField(fieldName);
        if (unsafe == null) {
            throw new NullPointerException("unsafe");
        }
        this.unsafe = unsafe;
        offset = unsafe.objectFieldOffset(field);
    }

    @Override
    public void set(U obj, W newValue) {
        unsafe.putObject(obj, offset, newValue);
    }

    @Override
    public W get(U obj) {
        return (W) unsafe.getObject(obj, offset);
    }
}
