package org.neptune.registry;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollSocketChannel;
import org.junit.jupiter.api.Test;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.common.util.ConcurrentSet;

public class ServiceInstanceTest {

    @Test
    public void testServiceInstance_HashCodeAndEquals() {
        ServiceInstance meta1 = serviceInstanceFactory(
                "127.0.0.1", 8000, 100, "name", "version","group",1
        );
        ServiceInstance meta2 = serviceInstanceFactory(
                "127.0.0.2", 8000, 100, "name" ,"version","group",1
        );
        ServiceInstance meta3 = serviceInstanceFactory(
                "127.0.0.1", 8001, 100, "name" ,"version","group",1
        );

        ServiceInstance meta4 = serviceInstanceFactory(
                "127.0.0.1", 8000, 101, "name" ,"version","group",1
        );
        ServiceInstance meta5 = serviceInstanceFactory(
                "127.0.0.1", 8000, 100, "name1" ,"version","group",1
        );
        ServiceInstance meta6 = serviceInstanceFactory(
                "127.0.0.1", 8000, 100, "name" ,"version1","group",1
        );
        ServiceInstance meta7 = serviceInstanceFactory(
                "127.0.0.1", 8000, 100, "name" ,"version","group1",1
        );
        ServiceInstance meta8 = serviceInstanceFactory(
                "127.0.0.1", 8000, 100, "name", "version","group",2
        );
        ConcurrentSet<ServiceInstance> metas = new ConcurrentSet<>();;
        metas.add(meta1);
        metas.add(meta2);
        metas.add(meta3);
        metas.add(meta4);
        metas.add(meta5);
        metas.add(meta6);
        metas.add(meta7);
        metas.add(meta8);
        System.out.println(metas.size());

        System.out.println(JSON.toJSONString(metas));
        assert metas.size() == 6;
    }

    private ServiceInstance serviceInstanceFactory(String host, int port, int wight, String serverName, String serverVersion, String group , int channelId){

        ServiceMeta serviceMeta = new ServiceMeta(
                serverName,serverVersion,group
        );
        UnresolvedAddress address =  new UnresolvedSocketAddress(host,port);
        ServiceInstance meta = new ServiceInstance();
        meta.setAddress(address);
        meta.setServiceMeta(serviceMeta);
        meta.setWight(wight);
        meta.setChannel(null);

        return meta;
    }



}
