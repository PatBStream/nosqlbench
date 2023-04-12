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
import scala.noinline;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

//import org.apache.kafka.clients.consumer.KafkaConsumer;
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
        String topicNameListStr = topicNameList.stream()
            .collect(Collectors.joining("::"));

        String cacheKey = S4RAdapterUtil.buildCacheKey(
            "consumer-" + String.valueOf(cycle % s4rClntCnt), topicNameListStr, groupId );

        OpTimeTrackS4RClient opTimeTrackS4RClient = s4RSpace.getOpTimeTrackS4RClient(cacheKey);
        if (opTimeTrackS4RClient == null) {
            Properties consumerConfProps = new Properties();
            consumerConfProps.putAll(consumerClientConfMap);
            consumerConfProps.put("group.id", groupId);
            // S4R Connection Factory for Pulsar
            ConnectionFactory s4rConnFactory = null;
            Connection s4rConnection = null;
            Channel s4rChannel = null;
            String consumerTag = null;
            String host = s4RSpace.getS4RClientConf().getS4rConnectionMap().get("host");
            String port = s4RSpace.getS4RClientConf().getS4rConnectionMap().get("port");
            logger.debug("S4R Pulsar host: " + host + " port: " + port);
            logger.debug("consumerClientConfMap Map is: " + consumerClientConfMap.toString());
            try {
                s4rConnFactory = new ConnectionFactory();
                s4rConnFactory.setHost(host);
                s4rConnFactory.setPort(Integer.parseInt(port));
                s4rConnection = s4rConnFactory.newConnection();
                s4rChannel = s4rConnection.createChannel();
                s4rChannel.queueDeclare("s4r_queue1", true, false, false, null);
                DefaultConsumer consumer = new S4RMessageHandler(s4rChannel);
                consumerTag = s4rChannel.basicConsume("s4r_queue1", true, consumer);
//                OpTimeTrackS4RClient opTimeTrackS4RClient = new OpTimeTrackS4RConsumer(
//                    this, false, 0,false,0, s4rChannel);
//                addOpTimeTrackS4RClient("1", opTimeTrackS4RClient);
            } catch (Exception e) {
                logger.error("Error creating new S4R Pulsar Connection: " + e.getMessage());
                e.printStackTrace();
            }

//            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfProps);
//            synchronized (this) {
//                consumer.subscribe(topicNameList);
//            }
            if (logger.isDebugEnabled()) {
                logger.debug("S4R consumer created: {}/{} -- {}, {}, {}",
                    cacheKey,
//                    consumer,
                    s4rChannel.getChannelNumber(),
                    topicNameList,
                    autoCommitEnabled,
                    maxMsgCntPerCommit);
            }
            synchronized (this) {
                int cnt;
                cnt = s4RSpace.getS4RClientsCount();
                cnt++;
                opTimeTrackS4RClient = new OpTimeTrackS4RConsumer(
//                    s4RSpace, asyncAPI, msgPollIntervalInSec, autoCommitEnabled, maxMsgCntPerCommit, consumer, channel);
                    s4RSpace, asyncAPI, msgPollIntervalInSec, autoCommitEnabled, maxMsgCntPerCommit, s4rChannel);
//                    s4RSpace, asyncAPI, msgPollIntervalInSec, autoCommitEnabled, maxMsgCntPerCommit, null);
                s4RSpace.addOpTimeTrackS4RClient(cacheKey, opTimeTrackS4RClient);
                s4RSpace.setS4RClientsCount(cnt);
                logger.info("Added new S4R client consumer tag: " + consumerTag + " key: " + cacheKey + " s4rClntCnt is: " + s4rClntCnt);
            }
        }
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
