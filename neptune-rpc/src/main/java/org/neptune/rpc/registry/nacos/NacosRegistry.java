package org.neptune.rpc.registry.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.SneakyThrows;
import org.neptune.rpc.ServiceMeta;
import org.neptune.rpc.registry.AbstractRegistry;
import org.neptune.rpc.registry.RegistryConnectMeta;
import org.neptune.rpc.registry.RegistryMeta;

import java.util.Map;
import java.util.Properties;

/**
 * @desc TODO
 *
 * @author tony
 * @createDate 2024/4/19 9:57 上午
 */
public class NacosRegistry extends AbstractRegistry {

    @Override
    public void register(RegistryMeta meta, RegisterListener listener) {
    }

    @Override
    public void unregister(RegistryMeta meta, RegisterListener listener) {
    }

    @Override
    public void register(RegistryMeta meta) throws Throwable {
    }

    @Override
    public void unregister(RegistryMeta meta) throws Throwable {
    }

    @Override
    public Map<Object, Integer> consumers() {
        return null;
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, RegistryNotifier notifier) {

    }

    @Override
    public void unsubscribe(ServiceMeta serviceMeta) {

    }

    @Override
    public void shutdownGracefully() {

    }

    public static void main(String[] args) throws Exception{
        Properties properties = new Properties();
        properties.put("serverAddr", "127.0.0.1:8848");
        NamingService naming = NacosFactory.createNamingService(properties);

        String serviceName = "example-service";

        naming.subscribe(serviceName, new EventListener() {
            @SneakyThrows
            @Override
            public void onEvent(Event event) {
                System.out.println("Service change detected: "+ new ObjectMapper().writeValueAsString(event));
            }
        });

        // Keep the application running to listen for events
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
