package org.neptune.registry;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.neptune.registry.nacos.NacosServiceSubscriber;

/**
 * @desc TODO
 *
 * @author tony
 * @createDate 2024/4/19 2:22 下午
 */
@Slf4j
public class NacosServiceSubscriberTest {
    @Test
    public void testSubscriber() throws Throwable {

        NacosServiceSubscriber serviceSubscriber = new NacosServiceSubscriber("127.0.0.1", "8848");
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServerVersion("1.0.0");
        serviceMeta.setServerName("demo-service");
        serviceMeta.setGroup("default");
        serviceSubscriber.subscribe(serviceMeta, null);
        log.info("完成订阅操作");

        Thread.sleep(1000);
    }
}
