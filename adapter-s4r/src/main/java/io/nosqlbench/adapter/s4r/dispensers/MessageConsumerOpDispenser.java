/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.s4r.dispensers;

import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterInvalidParamException;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.ops.S4ROp;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackS4RClient;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackS4RConsumer;
import io.nosqlbench.adapter.s4r.util.S4RAdapterUtil;
import io.nosqlbench.adapter.s4r.util.S4RMessageHandler;
import io.nosqlbench.adapter.s4r.util.S4RAdapterMetrics;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

public class MessageConsumerOpDispenser extends S4RBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("MessageConsumerOpDispenser");

    private final Map<String, String> consumerClientConfMap = new HashMap<>();

    // The timeout value as message Poll interval (in seconds)
    protected final int msgPollIntervalInSec;

    // Manual commit frequency
    // - # of received messages / sec.
    // - This is only relevant when the effective setting (global level and statement level)
    //   of "enable.auto.commit" is false
    protected final int maxMsgCntPerCommit;

    protected boolean autoCommitEnabled;

    public MessageConsumerOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> tgtNameFunc,
                                      S4RSpace s4RSpace) {
        super(adapter, op, tgtNameFunc, s4RSpace);

        this.consumerClientConfMap.putAll(s4RSpace.getS4RClientConf().getConsumerConfMap());
        consumerClientConfMap.put("bootstrap.servers", s4RSpace.getBootstrapSvr());

        this.msgPollIntervalInSec =
            NumberUtils.toInt(parsedOp.getStaticConfigOr("msg_poll_interval", "0"));

        this.maxMsgCntPerCommit =
            NumberUtils.toInt(parsedOp.getStaticConfig("manual_commit_batch_num", String.class));

        this.autoCommitEnabled = true;
        if (maxMsgCntPerCommit > 0) {
            this.autoCommitEnabled = false;
            consumerClientConfMap.put("enable.auto.commit", "false");
        } else {
            if (consumerClientConfMap.containsKey("enable.auto.commit")) {
                this.autoCommitEnabled = BooleanUtils.toBoolean(consumerClientConfMap.get("enable.auto.commit"));
            }
        }
    }

    private String getEffectiveGroupId(long cycle) {
        int grpIdx = (int) (cycle % consumerGrpCnt);
        String defaultGrpNamePrefix = S4RAdapterUtil.DFT_CONSUMER_GROUP_NAME_PREFIX;
        if (consumerClientConfMap.containsKey("group.id")) {
            defaultGrpNamePrefix = consumerClientConfMap.get("group.id");
        }

        return defaultGrpNamePrefix + "-" + grpIdx;
    }

    private OpTimeTrackS4RClient getOrCreateOpTimeTrackS4RConsumer(
        long cycle,
        List<String> topicNameList,
        String groupId)
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection connection;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("products_queue", false, false, false, null);
            DefaultConsumer consumer = new S4RMessageHandler(channel);
            channel.basicConsume("products_queue", true, consumer);
        } catch (IOException | TimeoutException e) {
            throw new S4RAdapterUnexpectedException("*** Error connecting to RabbitMQ Server: " + e.getMessage());
        }

        String topicNameListStr = topicNameList.stream()
            .collect(Collectors.joining("::"));

        String cacheKey = S4RAdapterUtil.buildCacheKey(
            "consumer-" + String.valueOf(cycle % s4rClntCnt), topicNameListStr, groupId );

        OpTimeTrackS4RClient opTimeTrackS4RClient = s4RSpace.getOpTimeTrackS4RClient(cacheKey);
        if (opTimeTrackS4RClient == null) {
            Properties consumerConfProps = new Properties();
            consumerConfProps.putAll(consumerClientConfMap);
            consumerConfProps.put("group.id", groupId);

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfProps);
            synchronized (this) {
                consumer.subscribe(topicNameList);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Kafka consumer created: {}/{} -- {}, {}, {}",
                    cacheKey,
                    consumer,
                    topicNameList,
                    autoCommitEnabled,
                    maxMsgCntPerCommit);
            }

            opTimeTrackS4RClient = new OpTimeTrackS4RConsumer(
                    s4RSpace, asyncAPI, msgPollIntervalInSec, autoCommitEnabled, maxMsgCntPerCommit, consumer, channel);
            s4RSpace.addOpTimeTrackS4RClient(cacheKey, opTimeTrackS4RClient);
        }
        logger.info("************  RabbitMQ Client Completed.**************************");
        return opTimeTrackS4RClient;
    }


    protected List<String> getEffectiveTopicNameList(long cycle) {
        String explicitTopicListStr = topicNameStrFunc.apply(cycle);
        assert (StringUtils.isNotBlank(explicitTopicListStr));

        return Arrays.stream(StringUtils.split(explicitTopicListStr, ','))
            .filter(s -> StringUtils.isNotBlank(s))
            .toList();
    }

    @Override
    public S4ROp apply(long cycle) {
        List<String> topicNameList = getEffectiveTopicNameList(cycle);
        String groupId = getEffectiveGroupId(cycle);
        if (topicNameList.size() ==0 || StringUtils.isBlank(groupId)) {
            throw new S4RAdapterInvalidParamException(
                "Effective consumer group name and/or topic names  are needed for creating a consumer!");
        }

        OpTimeTrackS4RClient opTimeTrackS4RConsumer =
            getOrCreateOpTimeTrackS4RConsumer(cycle, topicNameList, groupId);

        logger.debug("MessageConsumerOpsDispenser called, cycle #  " + cycle);
        return new S4ROp(
            s4rAdapterMetrics,
                s4RSpace,
            opTimeTrackS4RConsumer,
            null);
    } 
   
}
