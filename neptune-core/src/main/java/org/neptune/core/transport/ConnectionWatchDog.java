/*
 * Copyright (c) 2015 The Neptune Project
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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * org.neptune.core.connectionLayer.handlers - ConnectionWatchDog
 *
 * @author tony-is-coding
 * @date 2021/12/15 21:16
 */
public class ConnectionWatchDog extends ChannelInboundHandlerAdapter {

    private static final long BASIC_DELAY_MS = 2 << 4;
    private static final int MAX_RETRY = 2 << 3;
    private final Random random = new Random();

    private final ReconnectTask task = new ReconnectTask();
    private final Bootstrap bootstrap;
    private final Timer timer;
    private final SocketAddress remoteAddress;

    private int attempts;

    public ConnectionWatchDog(Bootstrap bootstrap, Timer timer, SocketAddress remoteAddress) {
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.remoteAddress = remoteAddress;
    }

    public ChannelHandler[] handlers() {
        return new ChannelHandler[0];
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        attempts = 0;
        super.channelActive(ctx);
    }

    private long backoffTime(int attempts) {
        return ((long) random.nextInt(2 << attempts)) * BASIC_DELAY_MS;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final boolean needReconnect = task.reconnect;
        if (needReconnect) {
            if (attempts < MAX_RETRY) {
                attempts++;
            }
            long timeout = backoffTime(attempts);
            timer.newTimeout(task, timeout, TimeUnit.MILLISECONDS);
        }
    }

    public void setReconnect(boolean reconnect) {
        task.setReconnect(reconnect);
    }

    class ReconnectTask implements TimerTask {
        private volatile boolean reconnect;

        @Override
        public void run(Timeout timeout) throws Exception {
            if (!reconnect) {
                System.out.println("do not need reconnect");
                return;
            }
            final ChannelFuture future;
            synchronized (bootstrap) {
                bootstrap.handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(handlers());
                    }
                });
                future = bootstrap.connect(remoteAddress);
            }
            future.addListener((ChannelFutureListener) f -> {
                final boolean succeed = f.isSuccess();
                if (!succeed) {
                    // 从头开始再次执行入栈的 channelInactive() 回调
                    f.channel().pipeline().fireChannelInactive();
                }
            });
        }

        public void setReconnect(boolean reconnect) {
            this.reconnect = reconnect;
        }
    }

}
