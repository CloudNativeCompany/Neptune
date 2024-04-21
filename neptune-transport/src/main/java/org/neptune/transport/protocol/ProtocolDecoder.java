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

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.Signal;
import lombok.extern.slf4j.Slf4j;
import org.neptune.transport.RequestPayload;
import org.neptune.transport.ResponsePayload;

import java.util.List;

/**
 * org.neptune.rpc.transportLayer.handlers - ProtocolDecoder
 * 不可共享, 存在 state的 全局一致性问题
 *
 * @author tony-is-coding
 * @date 2021/12/21 15:43
 */
@Slf4j
public class ProtocolDecoder extends ReplayingDecoder<ProtocolDecoder.State> {


    // 如果handler 被 share 是否存在风险, 需要考量一下
    private final ProtocolHeader header = new ProtocolHeader();

    public ProtocolDecoder() {
        super(State.MAGIC);
    }

    /**
     * 协议头解析, 通过 ReplayingDecoder进行便捷检查 + buffer优化
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    // out 列表 内的数据会被分多次调用fireChannelRead() 往下一个 inbound传递
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case MAGIC:
                ProtocolHeader.checkMagic(in.readShort());         // MAGIC
                checkpoint(State.SIGN);
            case SIGN:
                header.sign(in.readByte());         // 消息标志位
                checkpoint(State.STATUS);
            case STATUS:
                header.status(in.readByte());       // 状态位
                checkpoint(State.ID);
            case ID:
                header.invokeId(in.readLong());     // 消息id
                checkpoint(State.BODY_SIZE);
            case BODY_SIZE:
                header.bodySize(in.readInt());      // 消息体长度
                checkpoint(State.BODY);
            case BODY:
                switch (header.getMsgType()) {
                    case ProtocolHeader.REQUEST: {
                        int length = checkBodySize(header.getBodySize());
                        byte[] bytes = new byte[length];
                        in.readBytes(bytes);

                        RequestPayload payload = new RequestPayload(header.getInvokeId());
                        payload.setBytes(bytes);
                        payload.setSerialTypeCode(header.getSerialTypeCode());
                        out.add(payload);
                        break;
                    }
                    case ProtocolHeader.RESPONSE: {
                        int length = checkBodySize(header.getBodySize());
                        byte[] bytes = new byte[length];
                        in.readBytes(bytes);

                        ResponsePayload payload = new ResponsePayload(header.getInvokeId());
                        payload.setBytes(bytes);
                        payload.setStatus(header.getStatus());
                        payload.setSerialTypeCode(header.getSerialTypeCode());

                        out.add(payload);
                        break;
                    }
                    case ProtocolHeader.HEARTBEAT:
                        break;
                    default:
                        throw Signal.valueOf("illegal message type");
                }
                // 复原, 为了下一次读数据
                checkpoint(State.MAGIC);
        }
    }

    private static int checkBodySize(int size) throws Signal {
        if (size > ProtocolHeader.MAX_BODY_SIZE) {
            throw Signal.valueOf("body size too large"); // TODO: 自定义统一管理的异常
        }
        return size;
    }


    enum State {
        MAGIC,
        SIGN,
        STATUS,
        ID,
        BODY_SIZE,
        BODY
    }
}
