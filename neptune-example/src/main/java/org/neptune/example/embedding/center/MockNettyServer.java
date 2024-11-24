package org.neptune.example.embedding.center;

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
import org.neptune.registry.defaultimpl.DefaultRegistry;

@Slf4j
public class MockNettyServer {


    public static void main(String[] args) throws Exception {
        int port = 8001;
        DefaultRegistry defaultRegistry = new DefaultRegistry(
                "127.0.0.1", port
        );
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
