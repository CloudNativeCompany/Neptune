package org.neptune.registry.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neptune.registry.AbstractServiceSubscriber;
import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @desc TODO
 *
 * @author tony
 * @createDate 2024/4/19 12:15 下午
 */
public class NacosServiceSubscriber extends AbstractServiceSubscriber {

    NamingService namingService;

    public NacosServiceSubscriber(String addr, String port) {
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
    public void shutdownGracefully() {
        try {
            namingService.shutDown();
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, RegistryNotifier notifier) {
        String serviceName = serviceMeta.getAppName();
        try{
            List<Instance> allInstances = namingService.getAllInstances(serviceName);
            try {
                System.out.println(new ObjectMapper().writeValueAsString(allInstances));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            namingService.subscribe(serviceName, event -> {
                NamingEvent namedEvent = (NamingEvent) event;
                List<RegistryMeta> registeredMetas = new LinkedList<>();
                for (Instance instance : namedEvent.getInstances()) {
                    ServiceMeta s = new ServiceMeta();
                    s.setAppName(serviceName);
                    RegistryMeta meta = new RegistryMeta();

                    ServiceMeta sm = new ServiceMeta();
                    sm.setGroup(instance.getMetadata().get("group"));
                    sm.setVersion(instance.getMetadata().get("version"));
                    sm.setAppName(instance.getMetadata().get("appName"));

                    registeredMetas.add(meta);
                }
                updateServiceList(serviceMeta, registeredMetas);
            });
        }catch (NacosException e){
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(ServiceMeta serviceMeta) {

    }
}
