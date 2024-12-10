package org.neptune.registry;

import org.junit.jupiter.api.Test;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.common.util.ConcurrentSet;

public class RegistryMetaTest {

    @Test
    public void testRegistryMeta_HashCodeAndEquals() {
        RegistryMeta meta1 = registryMetaFactory(
                "127.0.0.1", 8000, 100, "name", "version","group"
        );
        RegistryMeta meta2 = registryMetaFactory(
                "127.0.0.2", 8000, 100, "name" ,"version","group"
        );
        RegistryMeta meta3 = registryMetaFactory(
                "127.0.0.1", 8001, 100, "name" ,"version","group"
        );

        RegistryMeta meta4 = registryMetaFactory(
                "127.0.0.1", 8000, 101, "name" ,"version","group"
        );
        RegistryMeta meta5 = registryMetaFactory(
                "127.0.0.1", 8000, 100, "name1" ,"version","group"
        );
        RegistryMeta meta6 = registryMetaFactory(
                "127.0.0.1", 8000, 100, "name" ,"version1","group"
        );
        RegistryMeta meta7 = registryMetaFactory(
                "127.0.0.1", 8000, 100, "name" ,"version","group1"
        );
        ConcurrentSet<RegistryMeta> metas = new ConcurrentSet<>();;
        metas.add(meta1);
        metas.add(meta2);
        metas.add(meta3);
        metas.add(meta4);
        metas.add(meta5);
        metas.add(meta6);
        metas.add(meta7);
        assert metas.size() == 6;
    }


    @Test
    public void testRegistryMeta_HashCodeAndEquals1() {
        RegistryMeta meta1 = registryMetaFactory(
                "127.0.0.1", 8000, 100, "name", "version","group"
        );
        RegistryMeta meta2 = registryMetaFactory(
                "127.0.0.1", 8000, 100, "name" ,"version","group"
        );

        RegistryMeta meta3 = registryMetaFactory(
                "127.0.0.1", 8000, 101, "name" ,"version","group"
        );

        ConcurrentSet<RegistryMeta> metas = new ConcurrentSet<>();;
        metas.add(meta1);
        metas.add(meta2);
        metas.add(meta3);
        assert metas.size() == 1;
    }

    private RegistryMeta registryMetaFactory(String host, int port, int wight,String serverName, String serverVersion, String group ){
        ServiceMeta serviceMeta = new ServiceMeta(
                serverName,serverVersion,group
        );

        UnresolvedAddress address =  new UnresolvedSocketAddress(host,port);

        RegistryMeta meta = new RegistryMeta();
        meta.setAddress(address);
        meta.setServiceMeta(serviceMeta);
        meta.setWight(wight);
        return meta;
    }


}
