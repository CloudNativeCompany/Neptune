package org.neptune.example;

import org.neptune.core.core.Client;
import org.neptune.core.core.DefaultClient;

/**
 * org.neptune.example - AutoClient
 *
 * @author tony-is-coding
 * @date 2021/12/17 20:26
 */
public class SyncClient {
    public static void main(String[] args) {
        Client client = new DefaultClient();
//        client.connectToRegistryServer("127.0.0.1:8080");
        client.connectTo("127.0.0.1:8080");
        try{
            Service service = client
                    .proxy(Service.class)
                    .newInstance();
            String result = service.call("hello world");
        }
        finally {
            client.shutdownGracefully();
        }

    }
}
