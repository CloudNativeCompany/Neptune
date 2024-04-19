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
package org.neptune.transport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.neptune.transport.RequestPayload;
import org.neptune.transport.ResponsePayload;

/**
 * org.neptune.rpc.transportLayer - ProtocolEncoder
 *
 * @author tony-is-coding
 * @date 2021/12/22 17:52
 */
@ChannelHandler.Sharable
public class ProtocolEncoder extends MessageToByteEncoder<Object> {
    /*
        TODO: 这个Handler需要兼容
            1. 请求出去的时候
            2. 响应出去的时候
            所以从本质上, 请求响应必须是一样的, 最好到这一层处理的已经是 byte[]了, 讲序列化的过程在业务线程做完
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof RequestPayload) {
            doEncodeRequest((RequestPayload) msg, out);
        } else if (msg instanceof ResponsePayload) {
            doEncodeResponse((ResponsePayload) msg, out);
        } else {
            throw new IllegalArgumentException();
        }
    }


    // 预分配buffer大小限制, 按照已知的头大小预分配, body大小此时尚未确认
    // 针对心跳类无消息体消息这样更节省空间
    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, @SuppressWarnings("unused") Object msg,
                                     boolean preferDirect) throws Exception {
        if (preferDirect) {
            return ctx.alloc().ioBuffer(ProtocolHeader.HEADER_SIZE);
        } else {
            return ctx.alloc().heapBuffer(ProtocolHeader.HEADER_SIZE);
        }
    }

    private void doEncodeRequest(RequestPayload request, ByteBuf out) {
        byte sign = ProtocolHeader.toSign(request.getSerialTypeCode(), ProtocolHeader.REQUEST);
        long invokeId = request.getXid();
        byte[] body = request.getBytes();
        int length = body.length;

        out.writeShort(ProtocolHeader.MAGIC_WORD)
                .writeByte(sign)
                .writeByte(0x00)
                .writeLong(invokeId)
                .writeInt(length)
                .writeBytes(body);
    }

    private void doEncodeResponse(ResponsePayload response, ByteBuf out) {
        byte sign = ProtocolHeader.toSign(response.getSerialTypeCode(), ProtocolHeader.RESPONSE);
        byte status = response.getStatus();
        long invokeId = response.getXid();
        byte[] body = response.getBytes();
        int length = body.length;

        out.writeShort(ProtocolHeader.MAGIC_WORD)
                .writeByte(sign)
                .writeByte(status)
                .writeLong(invokeId)
                .writeInt(length)
                .writeBytes(body);
    }

}
