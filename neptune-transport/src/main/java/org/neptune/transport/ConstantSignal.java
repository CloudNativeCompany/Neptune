package org.neptune.transport;

import io.netty.util.Signal;

public class ConstantSignal {
    public static final Signal ReadIdle = Signal.valueOf("Neptune-Read-Idle");

    public static final Signal WriteIdle = Signal.valueOf("Neptune-Write-Idle");
}
