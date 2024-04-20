package org.neptune.rpc.client;

import lombok.Builder;
import lombok.Getter;
import org.neptune.rpc.client.cluster.ClusterInvoker;
import org.neptune.rpc.client.lb.LoadBalancer;
import org.neptune.rpc.seialize.Serializer;

/**
 * @desc TODO
 *
 * @author tony
 * @createDate 2024/4/20 1:23 下午
 */
@Builder
@Getter
public class FactoryProxy {
    private final Serializer.SerializerType serializerType = Serializer.SerializerType.getDefault();
    private final LoadBalancer.LoadBalancerType loadBalancerType = LoadBalancer.LoadBalancerType.getDefault();
    private final ClusterInvoker.ClusterStrategy clusterStrategy = ClusterInvoker.ClusterStrategy.getDefault();
    private final boolean asyncInvoke = false;


    private static final FactoryProxy DEFAULT_FACTORY_PROXY = FactoryProxy.builder().build();

    public static FactoryProxy defaultFactoryProxy(){
        return DEFAULT_FACTORY_PROXY;
    }

}
