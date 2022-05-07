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
package org.neptune.common.util;

import org.neptune.common.unsafe.ReferenceFieldUpdater;
import org.neptune.common.unsafe.UnsafeUtil;
import org.neptune.common.unsafe.Updaters;
/**
 * @author tony-is-coding
 *  一个通用的异常抛出工具, 能够静默抛出异常, 这在一写 二方/三方包的 类方法重写时很有用
 *
 * @date 2021/12/17 16:15
 */
public final class ThrowUtil {

    private static final ReferenceFieldUpdater<Throwable, Throwable> causeUpdater =
            Updaters.newReferenceFieldUpdater(Throwable.class, "cause");

    /**
     * Raises an exception bypassing compiler checks for checked exceptions.
     */
    public static void throwException(Throwable t) {
        if (UnsafeUtil.hasUnsafe()) {
            UnsafeUtil.getUnsafeAccessor().throwException(t);
        } else {
            ThrowUtil.throwException0(t);
        }
    }

    /**
     * 类型转换只是骗过前端javac编译器, 泛型只是个语法糖, 在javac编译后会解除语法糖将类型擦除,
     * 也就是说并不会生成 checkcast 指令, 所以在运行期不会抛出ClassCastException异常
     *
     * private static <E extends java/lang/Throwable> void throwException0(java.lang.Throwable) throws E;
     *      flags: ACC_PRIVATE, ACC_STATIC
     *      Code:
     *      stack=1, locals=1, args_size=1
     *          0: aload_0
     *          1: athrow // 注意在athrow之前并没有checkcast指令
     *      ...
     *  Exceptions:
     *      throws java.lang.Throwable
     */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwException0(Throwable t) throws E {
        throw (E) t;
    }

    public static <T extends Throwable> T cutCause(T cause) {
        Throwable rootCause = cause;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        if (rootCause != cause) {
            cause.setStackTrace(rootCause.getStackTrace());
            causeUpdater.set(cause, cause);
        }
        return cause;
    }

    private ThrowUtil() {}
}
