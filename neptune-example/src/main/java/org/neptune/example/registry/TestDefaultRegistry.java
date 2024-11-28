package org.neptune.example.registry;

import org.neptune.registry.defaultimpl.DefaultRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

public class TestDefaultRegistry {
        private static final Logger logger = LoggerFactory.getLogger(TestDefaultRegistry.class.getName());


    public static void main(String[] args) throws InterruptedException {
        DefaultRegistry registry = new DefaultRegistry("127.0.0.1", 8001);
        registry.startServer();
    }

}
