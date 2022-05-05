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
package org.neptune.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ReaderHelper {
    private static final ReaderHelper helper = new ReaderHelper();
    private final AtomicInteger step = new AtomicInteger();

    private Integer incrStep() {
        return step.incrementAndGet();
    }

    public static void tip(String msg) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement callStack = stackTrace[2];
        String method = callStack.getClassName() + "." + callStack.getMethodName();
        String outMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                " [" + helper.incrStep() + "] " + method;
        if (msg != null && !"".equals(msg)) {
            outMsg = outMsg + " =>  " + msg;
        }
        StringBuilder threadInfo = new StringBuilder("" + Thread.currentThread().getName() + " - ");
        if (threadInfo.length() < 25) {
            int blank = 25 - threadInfo.length();
            for (int i = 0; i < blank; i++){
                threadInfo.append(" ");
            }
        }
        System.out.println(threadInfo + outMsg);
    }

    public static void formatTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement callStack = stackTrace[2];
        List<StackTraceElement> printTraces = Arrays
                .stream(stackTrace)
                .filter(e -> e.toString().startsWith("org.neptune.core"))
                .collect(Collectors.toList());
        if (printTraces.size() > 2) {
            printTraces = printTraces.subList(1, Math.min(10, printTraces.size()));
        }

        System.out.println("打印调用追踪: " + callStack.getClassName() + "." + callStack.getMethodName());
        String space = "|__";
        for (StackTraceElement traceElement : printTraces) {
            System.out.println(space + traceElement.toString());
            space = "  " + space;
        }
        System.out.println("\n\n");
    }

}
