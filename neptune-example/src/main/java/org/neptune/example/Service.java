package org.neptune.example;

import org.neptune.core.core.annotation.RpcService;

/**
 * org.neptune.example - ServiceRegistry
 *
 * @author tony-is-coding
 * @date 2021/12/20 15:37
 */
@RpcService(name = "service")
public interface Service {
    String call(String input);
}
