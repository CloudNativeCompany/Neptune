package org.neptune.example.registry;

import org.neptune.registry.defaultimpl.DefaultRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TestDefaultRegistry {
        private static final Logger logger = LoggerFactory.getLogger(TestDefaultRegistry.class.getName());


    public static void main(String[] args) throws InterruptedException {
        DefaultRegistry registry = new DefaultRegistry("127.0.0.1", 8001);
        registry.startServer();
    }

}
