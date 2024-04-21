package org.neptune.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockNettyServer {

    private int port;

    public MockNettyServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        // NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // 用于接收进来的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 用于处理已经被接收的连接
        try {
            ServerBootstrap b = new ServerBootstrap(); // 是一个启动NIO服务的辅助启动类
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 这里告诉Channel如何接收新的连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 添加自定义处理类
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // 设置TCP缓冲区
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // 保持连接

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync();

            // 等待服务器 socket 关闭 。
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8001;
        new MockNettyServer(port).run();
    }

    static class ServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        public void channelRead0(ChannelHandlerContext ctx, String msg) {
            log.info("Server received: " + msg);
            ctx.writeAndFlush(msg); // 将接收到的消息发送给发送者
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
