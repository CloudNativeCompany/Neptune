/*
 * Copyright (c) 2022 The Neptune Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neptune.core.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * org.neptune.core.transportLayer - SocketChannelFactoryProvider
 *
 * @author tony-is-coding
 * @date 2021/12/20 14:50
 */
public class SocketChannelFactoryProvider {

    public static final ChannelFactory<ServerChannel> JAVA_NIO_ACCEPTOR = NioServerSocketChannel::new;
    public static final ChannelFactory<ServerChannel> NATIVE_EPOLL_ACCEPTOR = EpollServerSocketChannel::new;
    public static final ChannelFactory<ServerChannel> NATIVE_KQUEUE_ACCEPTOR = KQueueServerSocketChannel::new;

    public static final ChannelFactory<Channel> JAVA_NIO_CONNECTOR = NioSocketChannel::new;
    public static final ChannelFactory<Channel> NATIVE_EPOLL_CONNECTOR = EpollSocketChannel::new;
    public static final ChannelFactory<Channel> NATIVE_KQUEUE_CONNECTOR = KQueueSocketChannel::new;

    public static ChannelFactory<Channel> connector(SocketType socketType) {
        switch (socketType) {
            case NATIVE_EPOLL:
                return NATIVE_EPOLL_CONNECTOR;
            case NATIVE_KQUEUE:
                return NATIVE_KQUEUE_CONNECTOR;
            default:
                return JAVA_NIO_CONNECTOR;
        }
    }

    public static ChannelFactory<ServerChannel> acceptor(SocketType socketType) {
        switch (socketType) {
            case NATIVE_EPOLL:
                return NATIVE_EPOLL_ACCEPTOR;
            case NATIVE_KQUEUE:
                return NATIVE_KQUEUE_ACCEPTOR;
            default:
                return JAVA_NIO_ACCEPTOR;
        }
    }

    public static SocketType socketType(boolean isNative) {
        if (isNative && NativeSupport.isNativeEPollAvailable()) {
            return SocketType.NATIVE_EPOLL;
        }
        if (isNative && NativeSupport.isNativeKQueueAvailable()) {
            return SocketType.NATIVE_KQUEUE;
        }
        return SocketType.JAVA_NIO;
    }

    public enum SocketType {
        JAVA_NIO,
        NATIVE_EPOLL,           // for linux
        NATIVE_KQUEUE,          // for bsd systems
    }

    public enum ChannelType {
        ACCEPTOR,
        CONNECTOR
    }


}
