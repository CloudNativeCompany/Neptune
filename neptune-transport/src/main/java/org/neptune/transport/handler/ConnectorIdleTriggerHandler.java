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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.neptune.transport.HeartBeatPayload;
import org.neptune.transport.RequestPayload;
import org.neptune.transport.seialize.Serializer;
import org.neptune.transport.seialize.SerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * org.neptune.rpc.transportLayer - ConnectorIdleTriggerHandler
 *
 * @author tony-is-coding
 * @date 2021/12/24 18:30
 */
public class ConnectorIdleTriggerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ConnectorIdleTriggerHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.info("userEventTriggered:{}", evt);
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {

                // write heartbeat to server
                Serializer serializer = SerializerFactory.getSerializer(Serializer.SerializerType.KRYO);
                HeartBeatPayload heartBeatPayload = new HeartBeatPayload(10001L);
                heartBeatPayload.setSerialTypeCode(serializer.typeCode());
                heartBeatPayload.setBytes(serializer.writeObject("ping"));

                ctx.channel().writeAndFlush(heartBeatPayload);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
