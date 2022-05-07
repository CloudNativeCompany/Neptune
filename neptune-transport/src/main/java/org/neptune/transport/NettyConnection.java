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
package org.neptune.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

/**
 * org.neptune.core.transportLayer - NettyConnection
 *
 * @author tony-is-coding
 * @date 2021/12/16 17:04
 */
public class NettyConnection implements Connection {

    private static final AttributeKey<NettyConnection> NETTY_CONNECTION_KEY = AttributeKey.valueOf("netty_connection");

    protected ChannelFuture future;
    protected SocketAddress remoteAddress;

    private boolean reconnect;
    private Channel channel;
    private boolean attacked = false;

    /**
     * now is maybe return a null
     *
     * @param channel
     * @return
     */
    public static NettyConnection linkedConnection(Channel channel) {
        Attribute<NettyConnection> attr = channel.attr(NETTY_CONNECTION_KEY);
        return attr.get();
    }

    private void linkTo(Channel channel) {
        if (!attacked) {
            attacked = true;
            this.channel = channel;
            channel.attr(NETTY_CONNECTION_KEY).set(this);
        } else {
            // todo: log this error of not needed?
        }
    }

    public NettyConnection(ChannelFuture future, SocketAddress remoteAddress) {
        this(future, remoteAddress, true);
    }

    public NettyConnection(ChannelFuture future, SocketAddress remoteAddress, boolean reconnect) {
        this.future = future;
        this.remoteAddress = remoteAddress;
        this.reconnect = reconnect;
        if (future.isSuccess()) {
            linkTo(future.channel());
        } else {
            future.addListener((ChannelFutureListener) f -> {
                linkTo(f.channel());
            });
        }
    }

    @Override
    public SocketAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public boolean needReconnect() {
        return reconnect;
    }

    @Override
    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    @Override
    public void disconnect() {
        // todo: disconnect
    }

    @Override
    public void onConnectCompleted(ChannelFutureListener listener) {
        if (!future.isSuccess()) {
            future.addListener(listener);
        }
    }

    @Override
    public Channel channel() {
        return channel;
    }
}
