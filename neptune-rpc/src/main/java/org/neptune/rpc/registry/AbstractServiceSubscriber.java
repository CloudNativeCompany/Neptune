package org.neptune.rpc.registry;

import org.neptune.rpc.ServiceMeta;

import java.util.Map;

/**
 * @desc TODO
 *
 * @author tony
 * @createDate 2024/4/19 11:35 上午
 */
public abstract class AbstractServiceSubscriber implements ServiceSubscriber{

    @Override
    public Map<Object, Integer> consumers() {
        return null;
    }
}
