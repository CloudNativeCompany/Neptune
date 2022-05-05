package org.neptune.example;

import org.neptune.core.core.DefaultServer;
import org.neptune.core.core.Server;
import org.neptune.core.core.ServiceMeta;
import org.neptune.core.core.ServiceProvider;

/**
 * org.neptune.example - SimpleServer
 *
 * @author tony-is-coding
 * @date 2021/12/22 12:28
 */
public class SimpleServer {
    public static void main(String[] args) {
        Server server = null;
        try {
            server = new DefaultServer();

            ServiceProvider provider = server.serviceProvider().
                    serviceMeta(new ServiceMeta())
                    .provider(new ServiceImpl())
                    .interfaceClass(Service.class);
            server.publish(provider);
            server.start();
        } catch (Exception e) {
        } finally {
            server.shutdownGracefully();
        }
    }
}
