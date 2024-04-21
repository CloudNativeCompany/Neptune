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
package org.neptune.transport.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

/**
 * org.neptune.rpc.transportLayer - NettyConnection
 *   NettyConnection
 * @author tony-is-coding
 * @date 2021/12/16 17:04
 */
public class NettyConnection implements Connection {

    private static final AttributeKey<NettyConnection> NETTY_CONNECTION_KEY = AttributeKey.valueOf("netty_connection");

    protected ChannelFuture future;
    protected SocketAddress remoteAddress;

    private ConnectFuture connectFuture;

    private boolean reconnect;
    private Channel channel;
    private boolean attacked = false;

    public NettyConnection(ChannelFuture future, SocketAddress remoteAddress) {
        this(future, remoteAddress, true);
    }

    public NettyConnection(ChannelFuture future, SocketAddress remoteAddress, boolean reconnect) {
        this.future = future;
        this.remoteAddress = remoteAddress;
        this.reconnect = reconnect;
        if (future.isSuccess()) {
            attackTo(future.channel());
        } else {
            future.addListener((ChannelFutureListener) f -> {
                attackTo(f.channel());
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


    private void attackTo(Channel channel) {
        System.out.println("connect succeed");
        // 考虑这个 attack是在两个场景 1. 首次连接 2.重新连接
        if (!attacked) {
            attacked = true;
            this.channel = channel;
            // 双向管理 -- 方便在 channel handler 阶段反向
            //这里考虑GC 是否有压力的问题
            channel.attr(NETTY_CONNECTION_KEY).set(this);
        } else {
            // todo: log this error of not needed?
        }
    }

    @Override
    public void disconnect() {
        // todo: disconnect
        attacked = false;
        connectFuture.onConnectClosed();
    }

    @Override
    public void addConnectFuture(ConnectFuture connectFuture) {
        this. connectFuture = connectFuture;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
