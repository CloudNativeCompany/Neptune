package org.neptune.rpc.registry.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.neptune.rpc.registry.RegistryConnectMeta;

import java.util.Properties;

/**
 * @desc TODO
 *
 * @author tony
 * @createDate 2024/4/19 10:04 上午
 */
public class NacosRegistryConnectMeta implements RegistryConnectMeta {

    @Override
    public String asConnectString() {
        return null;
    }

    public static void main(String[] args)  {

    }
}
