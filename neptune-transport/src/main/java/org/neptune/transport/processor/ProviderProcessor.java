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
package org.neptune.transport.processor;

import io.netty.channel.Channel;
import org.neptune.transport.RequestPayload;
import org.neptune.transport.Status;
import org.neptune.transport.connection.Connection;

/**
 * org.neptune.rpc.transportLayer - ProviderProcessor
 *
 * @author tony-is-coding
 * @date 2021/12/16 1:10
 */
public interface ProviderProcessor extends Processor {

    void handleRequest(Channel channel, RequestPayload request) throws Exception;

    /**
     * 处理异常
     */
    void handleException(Channel channel, RequestPayload request, Status status, Throwable cause);

}
