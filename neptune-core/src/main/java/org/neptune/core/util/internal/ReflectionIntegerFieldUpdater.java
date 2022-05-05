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
package org.neptune.core.util.internal;

import java.lang.reflect.Field;

/**
 *
 * @author tony-is-coding
 * @date 2021/12/17 16:15
 */
final class ReflectionIntegerFieldUpdater<U> implements IntegerFieldUpdater<U> {

    private final Field field;

    ReflectionIntegerFieldUpdater(Class<? super U> tClass, String fieldName) throws NoSuchFieldException {
        field = tClass.getDeclaredField(fieldName);
        field.setAccessible(true);
    }

    @Override
    public void set(U obj, int newValue) {
        try {
            field.set(obj, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int get(U obj) {
        try {
            return (Integer) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
