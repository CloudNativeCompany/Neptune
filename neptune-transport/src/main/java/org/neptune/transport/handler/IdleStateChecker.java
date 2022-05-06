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
package org.neptune.transport.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

/**
 * 基于 {@link HashedWheelTimer} 的空闲链路监测. 参考来源 {@link io.netty.handler.timeout.IdleStateHandler}
 * <p>
 * 1. Netty4.x默认的链路检测使用的是eventLoop的delayQueue, delayQueue是一个优先级队列, 复杂度为O(log n),
 * 每个worker处理自己的链路监测, 可能有助于减少上下文切换, 但是网络IO操作与idle会相互影响.
 * 2. 这个实现使用{@link HashedWheelTimer}的复杂度为O(1), 而且网络IO操作与idle不会相互影响, 但是有上下文切换.
 * 3. 如果连接数小, 比如几万以内, 可以直接用Netty4.x默认的链路检测 {@link io.netty.handler.timeout.IdleStateHandler},
 * 如果连接数较大, 建议使用这个实现.
 * <p>
 *
 * @author tony-is-coding
 * @date 2021/12/16 16:19
 */
public class IdleStateChecker extends ChannelDuplexHandler {
    /*
        README: 读写超时都是基于上一次的读或者写而言了, 每次读/写都需要进行超时(空闲)的重置
            考虑优化: 将读写超时抽象分离(虽然含义不大, 但是可读性增加)

        channel的基本生命周期: register -> active -> inactive -> unregister
        README: 什么时候需要进行空闲开始时间的重置?
             -
       README: 分离空闲检查, 作为connector端,只需要关注写空闲, 作为acceptor端则只需要关注读空闲

       README: 读写idle需要分别处理, 所以需要两个不同的handler来拦截 userEventTriggered
            - 读空闲, 代表远端(一般是client端)无消息发送过来, 可以尝试进行连接断开
            - 写空闲, 代表一段时间内未向远端(一般是server端)写入数据, 这时候需要发送心跳消息避免连接被kill
            心跳消息由客户端发起, 这样能一定程度上减少 server 端的压力(但是这并非完美的, 考虑 server - client 的数量持平, 每个client都连接了n个server)
     */
    private static final long MIN_TIMEOUT_MILLIS = 1;

    // do not create a new ChannelFutureListener per write operation to reduce GC pressure.
    private final ChannelFutureListener writeListener = new ChannelFutureListener() {

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            firstWriterIdleEvent = firstAllIdleEvent = true;
            lastWriteTime = System.currentTimeMillis(); // make hb for firstWriterIdleEvent and firstAllIdleEvent
        }
    };

    private final HashedWheelTimer timer; //README: 倒计时计算器, 滴答计时器

    private final long readerIdleTimeMillis;
    private final long writerIdleTimeMillis;
    private final long allIdleTimeMillis;

    private volatile int state; // 0 - none, 1 - initialized, 2 - destroyed
    private volatile boolean reading;

    private volatile Timeout readerIdleTimeout; //README: 超时管理, 里面包含了一个读超时任务
    private volatile long lastReadTime;
    private boolean firstReaderIdleEvent = true;

    private volatile Timeout writerIdleTimeout;  //README: 超时管理, 里面包含了一个写超时任务
    private volatile long lastWriteTime;
    private boolean firstWriterIdleEvent = true;

    private volatile Timeout allIdleTimeout; //README: 超时管理, 里面包含了一个写超时任务
    private boolean firstAllIdleEvent = true; // 允许首次超时不进行超时触发检测

    public IdleStateChecker(
            HashedWheelTimer timer,
            int readerIdleTimeSeconds,
            int writerIdleTimeSeconds,
            int allIdleTimeSeconds) {

        this(timer, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    public IdleStateChecker(
            HashedWheelTimer timer,
            long readerIdleTime,
            long writerIdleTime,
            long allIdleTime,
            TimeUnit unit) {

        if (unit == null) {
            throw new NullPointerException("unit");
        }

        this.timer = timer;

        if (readerIdleTime <= 0) {
            readerIdleTimeMillis = 0;
        } else {
            readerIdleTimeMillis = Math.max(unit.toMillis(readerIdleTime), MIN_TIMEOUT_MILLIS);
        }
        if (writerIdleTime <= 0) {
            writerIdleTimeMillis = 0;
        } else {
            writerIdleTimeMillis = Math.max(unit.toMillis(writerIdleTime), MIN_TIMEOUT_MILLIS);
        }
        if (allIdleTime <= 0) {
            allIdleTimeMillis = 0;
        } else {
            allIdleTimeMillis = Math.max(unit.toMillis(allIdleTime), MIN_TIMEOUT_MILLIS);
        }
    }

    /**
     * Return the readerIdleTime that was given when instance this class in milliseconds.
     */
    public long getReaderIdleTimeInMillis() {
        return readerIdleTimeMillis;
    }

    /**
     * Return the writerIdleTime that was given when instance this class in milliseconds.
     */
    public long getWriterIdleTimeInMillis() {
        return writerIdleTimeMillis;
    }

    /**
     * Return the allIdleTime that was given when instance this class in milliseconds.
     */
    public long getAllIdleTimeInMillis() {
        return allIdleTimeMillis;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel ch = ctx.channel();
        if (ch.isActive() && ch.isRegistered()) {
            // 动态添加时, 需要进行一次初始化, 一般动态添加只能发送在 出站/入站的操作行为中
            initialize(ctx);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        destroy();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // Initialize early if channel is active already.
        if (ctx.channel().isActive()) { // CONFUSE: channel会被注册到不同的 EventLoop? 源码不理解的地方?
            initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // This method will be invoked only if this handler was added
        // before channelActive() event is fired.  If a user adds this handler
        // after the channelActive() event, initialize() will be called by beforeAdd().

        // channel 创建时进行初始化
        initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (readerIdleTimeMillis > 0 || allIdleTimeMillis > 0) {
            firstReaderIdleEvent = firstAllIdleEvent = true;
            reading = true; // make hb for firstReaderIdleEvent and firstAllIdleEvent
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (readerIdleTimeMillis > 0 || allIdleTimeMillis > 0) {
            lastReadTime = System.currentTimeMillis(); // make hb for firstReaderIdleEvent and firstAllIdleEvent
            reading = false;
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (writerIdleTimeMillis > 0 || allIdleTimeMillis > 0) {
            if (promise.isVoid()) {
                firstWriterIdleEvent = firstAllIdleEvent = true;
                lastWriteTime = System.currentTimeMillis(); // make hb for firstWriterIdleEvent and firstAllIdleEvent
            } else {
                promise.addListener(writeListener);
            }
        }
        ctx.write(msg, promise);
    }

    private void initialize(ChannelHandlerContext ctx) {
        // Avoid the case where destroy() is called before scheduling timeouts.
        // See: https://github.com/netty/netty/issues/143
        switch (state) {
            case 1:
            case 2:
                return;
        }

        state = 1;

        lastReadTime = lastWriteTime = System.currentTimeMillis();
        if (readerIdleTimeMillis > 0) {
            readerIdleTimeout = timer.newTimeout(
                    new ReaderIdleTimeoutTask(ctx),
                    readerIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
        if (writerIdleTimeMillis > 0) {
            writerIdleTimeout = timer.newTimeout(
                    new WriterIdleTimeoutTask(ctx),
                    writerIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
        if (allIdleTimeMillis > 0) {
            allIdleTimeout = timer.newTimeout(
                    new AllIdleTimeoutTask(ctx),
                    allIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
    }

    private void destroy() {
        state = 2;

        if (readerIdleTimeout != null) {
            readerIdleTimeout.cancel();
            readerIdleTimeout = null;
        }
        if (writerIdleTimeout != null) {
            writerIdleTimeout.cancel();
            writerIdleTimeout = null;
        }
        if (allIdleTimeout != null) {
            allIdleTimeout.cancel();
            allIdleTimeout = null;
        }
    }

    //README: 连接channel 处于挂起状态时候被调用, 通过idle超时检测检测到
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    // README: 空闲监控,连接读超时则进行断路
    private final class ReaderIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled() || !ctx.channel().isOpen()) {
                return;
            }

            long lastReadTime = IdleStateChecker.this.lastReadTime;
            long nextDelay = readerIdleTimeMillis;
            if (!reading) {
                nextDelay -= System.currentTimeMillis() - lastReadTime;
            }
            if (nextDelay <= 0) {
                // Reader is idle - set a new timeout and notify the callback.
                readerIdleTimeout = timer.newTimeout(this, readerIdleTimeMillis, TimeUnit.MILLISECONDS);
                try {
                    IdleStateEvent event;
                    if (firstReaderIdleEvent) {
                        firstReaderIdleEvent = false;
                        event = IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.READER_IDLE_STATE_EVENT;
                    }
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // Read occurred before the timeout - set a new timeout with shorter delay.
                readerIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    // README: 超时监控,连接写超时则进行断路
    private final class WriterIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled() || !ctx.channel().isOpen()) {
                return;
            }

            long lastWriteTime = IdleStateChecker.this.lastWriteTime;
            long nextDelay = writerIdleTimeMillis - (System.currentTimeMillis() - lastWriteTime);
            if (nextDelay <= 0) {
                // Writer is idle - set a new timeout and notify the callback.
                writerIdleTimeout = timer.newTimeout(this, writerIdleTimeMillis, TimeUnit.MILLISECONDS);
                try {
                    IdleStateEvent event;
                    if (firstWriterIdleEvent) {
                        firstWriterIdleEvent = false;
                        event = IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.WRITER_IDLE_STATE_EVENT;
                    }
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // Write occurred before the timeout - set a new timeout with shorter delay.
                writerIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    private final class AllIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        AllIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled() || !ctx.channel().isOpen()) {
                return;
            }

            long nextDelay = allIdleTimeMillis;
            if (!reading) {
                long lastIoTime = Math.max(lastReadTime, lastWriteTime);
                nextDelay -= System.currentTimeMillis() - lastIoTime;
            }
            if (nextDelay <= 0) {
                // Both reader and writer are idle - set a new timeout and
                // notify the callback.
                allIdleTimeout = timer.newTimeout(this, allIdleTimeMillis, TimeUnit.MILLISECONDS);
                try {
                    IdleStateEvent event;
                    if (firstAllIdleEvent) { // 首次触发后
                        firstAllIdleEvent = false;
                        event = IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.ALL_IDLE_STATE_EVENT;
                    }
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // Either read or write occurred before the timeout - set a new
                // timeout with shorter delay.
                allIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }
}
