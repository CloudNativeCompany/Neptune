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

import org.neptune.core.core.Directory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

import static org.neptune.core.transport.SocketChannelFactoryProvider.*;

/**
 * org.neptune.core.transportLayer - NettyConnector
 * 抽象的 netty 连接器
 * 1. 进行 netty bootstrap 初始化能力
 * 2.
 *
 * @author tony-is-coding
 * @date 2021/12/16 1:13
 */
public abstract class NettyConnector implements Connector {

    // timer, 主要是做超时重连的, 但我认为是任意层面/协议 的重连都需要 timer 计时器
    protected final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("connector.timer", true));
    protected final ConcurrentHashMap<String, CowConnectionGroupList> directoryGroupListMap = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<UnresolvedAddress, ConnectionGroup> addressGroupMap = new ConcurrentHashMap<>();

    // netty
    private final Bootstrap bootstrap;
    private final EventLoopGroup workers;
    private final SocketType socketType;

    protected NettyConnector(int workerNum, boolean isNative) {
        this.socketType = SocketChannelFactoryProvider.socketType(isNative);
        workers = createEventLoopGroup(workerNum, new DefaultThreadFactory("rpc.connect")); // 默认用10个, 后续配置应按照系统识别默认, 且支持参数配置化
        bootstrap = new Bootstrap().group(workers);
        doInit();
    }

    @Override
    public CowConnectionGroupList find(Directory directory) {
        return directoryGroupListMap.get(directory.directoryString());
    }

    @Override
    public ConnectionGroup group(UnresolvedAddress address) {
        ConnectionGroup group = addressGroupMap.get(address);
        if(group == null){
            ConnectionGroup newGroup = new ConnectionGroup();
            group = addressGroupMap.putIfAbsent(address,newGroup); // 考虑到 new到put 非原子性问题, 这样做能少一次加锁
            if(group == null){
                group = newGroup;
            }
        }
        return group;
    }

    public Timer timer() {
        return this.timer;
    }

    protected SocketType socketType() {
        return this.socketType;
    }

    protected Bootstrap bootstrap() {
        return this.bootstrap;
    }

    /**
     * 1. 初始化ChannelOption 配置
     * 2. 初始化ChannelFactory 配置
     */
    protected abstract void doInit();

    /*
        配置是作用与每个连接的, 每次创建连接都可以进行设置参数
     */
    protected void setOptions() {
        // TODO 公共配置抽象拓展
    }

    protected abstract EventLoopGroup createEventLoopGroup(int nThreads, ThreadFactory factory);

    @Override
    public void shutdownGracefully() {
        workers.shutdownGracefully();
    }
}
