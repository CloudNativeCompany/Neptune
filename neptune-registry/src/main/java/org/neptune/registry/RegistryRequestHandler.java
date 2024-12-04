package org.neptune.registry;

import io.netty.channel.Channel;

public interface RegistryRequestHandler {
    RegistryResponse handle(Channel channel, RegistryRequest request);
}
