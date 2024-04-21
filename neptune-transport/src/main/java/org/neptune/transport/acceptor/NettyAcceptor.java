package org.neptune.transport.acceptor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.neptune.common.UnresolvedAddress;
import org.neptune.common.UnresolvedSocketAddress;
import org.neptune.transport.handler.AcceptorHandler;
import org.neptune.transport.handler.AcceptorIdleTriggerHandler;
import org.neptune.transport.handler.IdleStateChecker;
import org.neptune.transport.processor.ProviderProcessor;
import org.neptune.transport.protocol.ProtocolDecoder;
import org.neptune.transport.protocol.ProtocolEncoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @desc TODO
 *
 * @author tony
 * @createDate 2024/4/21 12:31 下午
 */
@Slf4j
public class NettyAcceptor implements Acceptor{


    // timer, 主要是做超时重连的, 但我认为是任意层面/协议 的重连都需要 timer 计时器
    protected final HashedWheelTimer timer = new HashedWheelTimer(new DefaultThreadFactory("neptune.server.timer", true));

    private static final String IP_ADDRESS = SystemPropertyUtil.get("neptune.server.localAddress", NetUtil.LOCALHOST4.getHostAddress());
    private static final int BOSS_THREAD_NUM = SystemPropertyUtil.getInt("neptune.server.bossThreadNum", 4);
    private static final int WORKER_THREAD_NUM = SystemPropertyUtil.getInt("neptune.server.workerThreadNum", 16);

    private final UnresolvedAddress address;
    private final SocketAddress socketAddress;
    private ProviderProcessor providerProcessor = null;

    private final int nBosses;
    private final int nWorkers;

    private ServerBootstrap bootstrap;
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    public NettyAcceptor(int port) {
        // 服务端给的默认线程数
        this(BOSS_THREAD_NUM,WORKER_THREAD_NUM, port);
    }

    public NettyAcceptor(int nBosses, int nWorkers, int port) {
        this.address = new UnresolvedSocketAddress(IP_ADDRESS, port);
        this.socketAddress = new InetSocketAddress(port);
        this.nBosses = nBosses;
        this.nWorkers = nWorkers;
        init();
    }

    public void withProcessor(ProviderProcessor processor){
        this.providerProcessor = processor;
    }

    @Override
    public UnresolvedAddress resolvedAddress() {
        return address;
    }

    @Override
    public SocketAddress localAddress() {
        return socketAddress;
    }

    @Override
    public void startAsync() throws Exception {
        start(true);
    }

    @Override
    public void start(boolean sync) throws Exception {
        bootstrap
                .childHandler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(
                        new FlushConsolidationHandler(5, true), // 合并发送, 每5次之后再进行一次真正的网络发发送
                        new IdleStateChecker(timer, 5, 5, 60),
                        new AcceptorIdleTriggerHandler(),
                        new ProtocolDecoder(),
                        new ProtocolEncoder(),
                        new AcceptorHandler(providerProcessor)
                );

            }
        });

        log.info("bind port to: " + address.port() + " success!!!");
        ChannelFuture bindFuture = bootstrap.bind(localAddress()).sync();

        // 直到关闭
        bindFuture.channel().closeFuture().sync();
    }


    @Override
    public void shutdownGracefully() {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }

    protected void init() {
        boss = new NioEventLoopGroup(nBosses, new DefaultThreadFactory("neptune-acceptor-boss", Thread.MAX_PRIORITY));
        worker = new NioEventLoopGroup(nWorkers, new DefaultThreadFactory("neptune-acceptor-worker", Thread.MAX_PRIORITY));
        bootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(boss, worker)
                .option(ChannelOption.SO_BACKLOG, 128)          // 设置TCP缓冲区
                .childOption(ChannelOption.SO_KEEPALIVE, true); // 保持连接
        ;
        // optional 优化 -- 针对 netty 的
    }

}
