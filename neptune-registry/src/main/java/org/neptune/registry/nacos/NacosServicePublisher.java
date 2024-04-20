package org.neptune.registry.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.neptune.registry.AbstractServicePublisher;
import org.neptune.registry.RegistryMeta;
import org.neptune.registry.ServiceMeta;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @desc
 *
 * @author tony
 * @createDate 2024/4/19 11:34 上午
 */
public class NacosServicePublisher extends AbstractServicePublisher {


    private final NamingService namingService;

    public NacosServicePublisher(String addr, String port) {
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
    public void register(RegistryMeta meta, RegisterListener listener) {
        try{
            Instance instance = new Instance();
            ServiceMeta serviceMeta = meta.getServiceMeta();

            String serviceName = serviceMeta.getAppName();
            Map<String,String> metadata = new LinkedHashMap<>();

            metadata.put("group", serviceMeta.getGroup());
            metadata.put("version", serviceMeta.getAppVersion());
            metadata.put("appName", serviceMeta.getAppName());

            instance.setIp(meta.getAddress().host());
            instance.setPort(meta.getAddress().port());
            instance.setInstanceId(meta.toUniqueInstanceId());
            instance.setServiceName(serviceName);
            instance.setMetadata(metadata);

            instance.setWeight(meta.getWight()); // 设置权重
            instance.setHealthy(true);
            // 向 Nacos 注册服务实例
            namingService.registerInstance(serviceName, instance);
            listener.onCompleted();
        }catch (NacosException e){
            listener.onFailure();
            throw new RuntimeException("register error");
        }
    }

    @Override
    public void unregister(RegistryMeta meta, RegisterListener listener) {
        try{
            Instance instance = new Instance();
            ServiceMeta serviceMeta = meta.getServiceMeta();

            String serviceName = serviceMeta.getAppName();
            Map<String,String> metadata = new LinkedHashMap<>();

            metadata.put("group", serviceMeta.getGroup());
            metadata.put("version", serviceMeta.getAppVersion());
            metadata.put("appName", serviceMeta.getAppName());

            instance.setIp(meta.getAddress().host());
            instance.setPort(meta.getAddress().port());
            instance.setInstanceId(meta.toUniqueInstanceId());
            instance.setServiceName(serviceName);
            instance.setHealthy(false);
            namingService.deregisterInstance(serviceName, instance);
            listener.onCompleted();
        }catch (NacosException e){
            listener.onFailure();
            throw new RuntimeException("deregister error");
        }
    }

    private static final RegisterListener NOP_REGISTER_LISTENER = new RegisterListener() {
        @Override
        public void onCompleted() {
        }
        @Override
        public void onFailure() {
        }
    };

    @Override
    public void register(RegistryMeta meta) throws Throwable {
        register(meta,NOP_REGISTER_LISTENER);
    }

    @Override
    public void unregister(RegistryMeta meta) throws Throwable {
        unregister(meta, NOP_REGISTER_LISTENER);
    }

    @Override
    public void shutdownGracefully() {
        try{
            namingService.shutDown();
        }catch (Exception e){
            System.out.println("shut down error");
        }
    }


}
