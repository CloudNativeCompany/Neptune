package org.neptune.example.registry;

import org.neptune.registry.defaultimpl.DefaultPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class TestDefaultPublisher {

    private static final Logger logger = LoggerFactory.getLogger(TestDefaultPublisher.class.getName());
    public static void main(String[] args) {

        DefaultPublisher defaultPublisher = new DefaultPublisher("127.0.0.1", 8001);


    }

}
