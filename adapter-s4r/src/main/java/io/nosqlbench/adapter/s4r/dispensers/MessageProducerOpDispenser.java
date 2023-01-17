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
import io.nosqlbench.adapter.s4r.ops.S4ROp;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackS4RClient;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackS4RProducer;
import io.nosqlbench.adapter.s4r.util.S4RAdapterUtil;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.LongFunction;

public class MessageProducerOpDispenser extends S4RBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("MessageProducerOpDispenser");

    public static final String MSG_HEADER_OP_PARAM = "msg_header";
    public static final String MSG_KEY_OP_PARAM = "msg_key";
    public static final String MSG_BODY_OP_PARAM = "msg_body";

    private final Map<String, String> producerClientConfMap = new HashMap<>();

    protected final int txnBatchNum;
    private final LongFunction<String> msgHeaderJsonStrFunc;
    private final LongFunction<String> msgKeyStrFunc;
    private final LongFunction<String> msgValueStrFunc;

    public MessageProducerOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> tgtNameFunc,
                                      S4RSpace s4RSpace) {
        super(adapter, op, tgtNameFunc, s4RSpace);

        this.producerClientConfMap.putAll(s4RSpace.getS4RClientConf().getProducerConfMap());
        producerClientConfMap.put("bootstrap.servers", s4RSpace.getBootstrapSvr());

        this.txnBatchNum = parsedOp.getStaticConfigOr("txn_batch_num", Integer.valueOf(0));

        this.msgHeaderJsonStrFunc = lookupOptionalStrOpValueFunc(MSG_HEADER_OP_PARAM);
        this.msgKeyStrFunc = lookupOptionalStrOpValueFunc(MSG_KEY_OP_PARAM);
        this.msgValueStrFunc = lookupMandtoryStrOpValueFunc(MSG_BODY_OP_PARAM);
    }

    private String getEffectiveClientId(long cycle) {
        if (producerClientConfMap.containsKey("client.id")) {
            String defaultClientIdPrefix = producerClientConfMap.get("client.id");
            int clntIdx = (int) (cycle % s4rClntCnt);

            return defaultClientIdPrefix + "-" + clntIdx;
        }
        else {
            return "";
        }
    }

    private OpTimeTrackS4RClient getOrCreateOpTimeTrackS4RProducer(long cycle,
                                                                       String topicName,
                                                                       String clientId)
    {
        String cacheKey = S4RAdapterUtil.buildCacheKey(
            "producer-" + String.valueOf(cycle % s4rClntCnt), topicName);

        OpTimeTrackS4RClient opTimeTrackS4RClient = s4RSpace.getOpTimeTrackS4RClient(cacheKey);
        if (opTimeTrackS4RClient == null) {
            Properties producerConfProps = new Properties();
            producerConfProps.putAll(producerClientConfMap);

            if (StringUtils.isNotBlank(clientId))
                producerConfProps.put("client.id", clientId);
            else
                producerConfProps.remove("client.id");

            // When transaction batch number is less than 2, it is treated effectively as no-transaction
            if (txnBatchNum < 2)
                producerConfProps.remove("transactional.id");

            String baseTransactId = "";
            boolean transactionEnabled = false;
            if (producerConfProps.containsKey("transactional.id")) {
                baseTransactId = producerConfProps.get("transactional.id").toString();
                producerConfProps.put("transactional.id", baseTransactId + "-" + cacheKey);
                transactionEnabled = StringUtils.isNotBlank(producerConfProps.get("transactional.id").toString());
            }

            KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfProps);
            if (transactionEnabled) {
                producer.initTransactions();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Producer created: {}/{} -- ({}, {}, {})",
                    cacheKey,
                    producer,
                    topicName,
                    transactionEnabled,
                    clientId);
            }

            opTimeTrackS4RClient = new OpTimeTrackS4RProducer(
                    s4RSpace,
                asyncAPI,
                transactionEnabled,
                txnBatchNum,
                producer);
            s4RSpace.addOpTimeTrackS4RClient(cacheKey, opTimeTrackS4RClient);
        }

        return opTimeTrackS4RClient;
    }

    private ProducerRecord<String, String> createKafkaMessage(
        long curCycle,
        String topicName,
        String msgHeaderRawJsonStr,
        String msgKey,
        String msgValue
    ) {
        if (StringUtils.isAllBlank(msgKey, msgValue)) {
            throw new S4RAdapterInvalidParamException("Message key and value can't both be empty!");
        }

        int messageSize = S4RAdapterUtil.getStrObjSize(msgKey) + S4RAdapterUtil.getStrObjSize(msgValue);

        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, msgKey, msgValue);

        // Check if msgHeaderRawJsonStr is a valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message headers without throwing a runtime exception
        Map<String, String> msgHeaderProperties = new HashMap<>();
        if (!StringUtils.isBlank(msgHeaderRawJsonStr)) {
            try {
                msgHeaderProperties = S4RAdapterUtil.convertJsonToMap(msgHeaderRawJsonStr);
            } catch (Exception e) {
                logger.warn(
                    "Error parsing message property JSON string {}, ignore message properties!",
                    msgHeaderRawJsonStr);
            }
        }

        for (Map.Entry<String, String> entry : msgHeaderProperties.entrySet()) {
            String headerKey = entry.getKey();
            String headerValue = entry.getValue();

            messageSize += S4RAdapterUtil.getStrObjSize(headerKey) + S4RAdapterUtil.getStrObjSize(headerValue);

            if (! StringUtils.isAnyBlank(headerKey, headerValue)) {
                record.headers().add(headerKey, headerValue.getBytes());
            }

        }

        // NB-specific headers
        messageSize += S4RAdapterUtil.getStrObjSize(S4RAdapterUtil.NB_MSG_SEQ_PROP);
        messageSize += 8;
        messageSize += S4RAdapterUtil.getStrObjSize(S4RAdapterUtil.NB_MSG_SIZE_PROP);
        messageSize += 6;

        record.headers().add(S4RAdapterUtil.NB_MSG_SEQ_PROP, String.valueOf(curCycle).getBytes());
        record.headers().add(S4RAdapterUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize).getBytes());

        return record;
    }

    @Override
    public S4ROp apply(long cycle) {
        String topicName = topicNameStrFunc.apply(cycle);
        String clientId = getEffectiveClientId(cycle);

        OpTimeTrackS4RClient opTimeTrackS4RProducer =
            getOrCreateOpTimeTrackS4RProducer(cycle, topicName, clientId);

        ProducerRecord<String, String> message = createKafkaMessage(
            cycle,
            topicName,
            msgHeaderJsonStrFunc.apply(cycle),
            msgKeyStrFunc.apply(cycle),
            msgValueStrFunc.apply(cycle)
        );

        return new S4ROp(
            s4rAdapterMetrics,
                s4RSpace,
            opTimeTrackS4RProducer,
            message);
    }
}
