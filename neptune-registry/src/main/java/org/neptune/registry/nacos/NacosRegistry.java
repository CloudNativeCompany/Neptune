package org.neptune.registry.nacos;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.common.util.ConcurrentSet;
import org.neptune.registry.AbstractRegistry;
import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;
import org.neptune.registry.ServiceSubscriber;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc
 *
 * @author tony
 * @createDate 2024/4/19 11:34 上午
 */
@Slf4j
public class NacosRegistry extends AbstractRegistry {

    private final NamingService namingService;

    public NacosRegistry(String addr, String port) {
        Properties properties = new Properties();
        properties.put("serverAddr", addr + ":"+port); // Nacos 服务器地址
        properties.put("namespace", "public"); // 如有需要，指定命名空间
        try {
            namingService = NacosFactory.createNamingService(properties);
        }catch (NacosException e){
            throw  new RuntimeException("can not connect to nacos");
        }
    }

    @Override
    protected void actionAfterRegister() {

    }

    @Override
    protected void actionAfterSubscribe(ServiceMeta serviceMeta, ServiceSubscriber.RegistryNotifier notifier) {
        String serviceName = serviceMeta.getServerName();
        try{
            namingService.subscribe(serviceName, event -> {
                NamingEvent namedEvent = (NamingEvent) event;
                log.info("event trigger: " + JSON.toJSONString(event));
                List<RegistryMeta> registeredMetas = new LinkedList<>();
                for (Instance instance : namedEvent.getInstances()) {
                    RegistryMeta meta = new RegistryMeta();

                    ServiceMeta sm = new ServiceMeta();
                    sm.setGroup(instance.getMetadata().get("group"));
                    sm.setServerVersion(instance.getMetadata().get("version"));
                    sm.setServerName(instance.getMetadata().get("appName"));

                    UnresolvedAddress addr = new UnresolvedSocketAddress(instance.getIp(), instance.getPort());

                    meta.setWight((int)instance.getWeight());
                    meta.setAddress(addr);
                    meta.setServiceMeta(sm);

                    registeredMetas.add(meta);
                    // todo: 如何计算是节点的什么事件
                    notifier.notify(meta, ServiceSubscriber.RegistryNotifier.EventType.SERVICE_ADDED);
                }
                updateServiceList(serviceMeta, registeredMetas);
            });
        }catch (NacosException e){
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownGracefully() {
        try{
            namingService.shutDown();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
