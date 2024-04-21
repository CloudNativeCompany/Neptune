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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.neptune.transport.ResponsePayload;
import org.neptune.transport.processor.ConsumerProcessor;

/**
 * org.neptune.rpc.transportLayer - ResponseHandler
 *
 * @author tony-is-coding
 * @date 2021/12/24 16:32
 */
@ChannelHandler.Sharable
public class ResponseHandler extends ChannelInboundHandlerAdapter {

    private ConsumerProcessor processor;

    public ResponseHandler(ConsumerProcessor processor) {
        this.processor = processor;
    }

    // 最后一个channelRead 需要进行显示的 buffer 池化与释放操作
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("ResponseHandler收到响应结果并开始处理：" + msg);
        final Channel channel = ctx.channel();
        if (msg instanceof ResponsePayload) {
            processor.handlerResponse(channel, (ResponsePayload) msg);
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    public ConsumerProcessor processor() {
        return processor;
    }

}
