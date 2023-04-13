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

package io.nosqlbench.adapter.s4r;

import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackS4RClient;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackS4RConsumer;
import io.nosqlbench.adapter.s4r.util.S4RAdapterUtil;
import io.nosqlbench.adapter.s4r.util.S4RClientConf;
import io.nosqlbench.adapter.s4r.util.S4RMessageHandler;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Connection;

public class S4RSpace implements  AutoCloseable {

    private final static Logger logger = LogManager.getLogger(S4RSpace.class);

    private final String spaceName;
    private final NBConfiguration cfg;

    private final ConcurrentHashMap<String, OpTimeTrackS4RClient> opTimeTrackS4RClients = new ConcurrentHashMap<>();

    private final String bootstrapSvr;
    private final String s4rClientConfFileName;
    private final S4RClientConf s4rClientConf;

    // Whether to do strict error handling while sending/receiving messages
    // - Yes: any error returned from the Pulsar server while doing message receiving/sending will trigger NB execution stop
    // - No: pause the current thread that received the error message for 1 second and then continue processing
    private final boolean strictMsgErrorHandling;

    // Maximum time length to execute S4J operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. S4JOp is always executed until NB execution cycle finishes
    private final long maxOpTimeInSec;
    private final long activityStartTimeMills;

    // Maximum number of Kafka clients
    // - For Producer workload, this represents how many total producers to publish messages
    //   it must be the same value as the NB "threads" parameter
    // - For Consumer workload, this represents how many total consumers per consumer group to subscribe messages
    private final int s4rClntNum;

    // Maximum number of Kafka consumer groups
    // - Only relevant for Consumer workload
    // - (topicPartNum * consumerGrpNum) is the total consumer thread number and must be the same
    //   as the NB "threads" parameter
    // - For multi-topic testing, this means one consumer thread may read from multiple topics.
    private final int consumerGrpNum;

    private long totalCycleNum;
    // S4R Connection Factory for Pulsar
    private ConnectionFactory s4rConnFactory;
    private Connection s4rConnection;
    private Channel s4rChannel;
    private int s4rClientsCount = 0;
    private AtomicBoolean beingShutdown = new AtomicBoolean(false);

    public S4RSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
        this.cfg = cfg;

        this.bootstrapSvr = cfg.get("bootstrap_server");
        this.s4rClntNum =
            NumberUtils.toInt(cfg.getOptional("num_clnt").orElse("1"));
        this.consumerGrpNum =
            NumberUtils.toInt(cfg.getOptional("num_cons_grp").orElse("1"));
        this.maxOpTimeInSec =
            NumberUtils.toLong(cfg.getOptional("max_op_time").orElse("0L"));
        this.strictMsgErrorHandling =
            BooleanUtils.toBoolean(cfg.getOptional("strict_msg_error_handling").orElse("false"));
        this.s4rClientConfFileName = cfg.get("config");
        this.s4rClientConf = new S4RClientConf(s4rClientConfFileName);
        this.activityStartTimeMills = System.currentTimeMillis();
//        this.initializeSpace(s4rClientConf);
        logger.info("Initializing S4RSpace Name: " + spaceName + " Connection Config Maps Values: " + s4rClientConf.getS4rConnectionMap().toString());
    }

    @Override
    public void close() {
        shutdownSpace();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(S4RSpace.class)
            .add(Param.defaultTo("bootstrap_server", "pulsar://localhost:9020")
                .setDescription("Kafka bootstrap server URL."))
            .add(Param.defaultTo("config", "config.properties")
                .setDescription("Kafka client connection configuration property file."))
            .add(Param.defaultTo("num_clnt", 1)
                .setDescription("Number of S4R clients. For consumer, this is the number of consumers per consumer group"))
            .add(Param.defaultTo("num_cons_grp", 1)
                .setDescription("Number of consumer groups (only relevant for S4R consumer workload). "))
            .add(Param.defaultTo("max_op_time", 0)
                .setDescription("Maximum time (in seconds) to run NB S4R testing scenario."))
            .add(Param.defaultTo("strict_msg_error_handling", false)
                .setDescription("Whether to do strict error handling which is to stop NB Kafka execution."))
            .asReadOnly();
    }

    public OpTimeTrackS4RClient getOpTimeTrackS4RClient(String cacheKey) {
        return opTimeTrackS4RClients.get(cacheKey);
    }
    public void addOpTimeTrackS4RClient(String cacheKey, OpTimeTrackS4RClient client) {
        opTimeTrackS4RClients.put(cacheKey, client);
    }

    public long getActivityStartTimeMills() { return this.activityStartTimeMills; }
    public long getMaxOpTimeInSec() { return this.maxOpTimeInSec; }
    public String getBootstrapSvr() { return this.bootstrapSvr; }
    public S4RClientConf getS4RClientConf() { return s4rClientConf; }

    public int getS4RClntNum() { return this.s4rClntNum; }
    public int getConsumerGrpNum() { return this.consumerGrpNum; }

    public boolean isStrictMsgErrorHandling() { return  this.strictMsgErrorHandling; }

    public long getTotalCycleNum() { return totalCycleNum; }
    public void setTotalCycleNum(long cycleNum) { totalCycleNum = cycleNum; }
    
    public int getS4RClientsCount() {return s4rClientsCount; }
    public void setS4RClientsCount(int cnt) {s4rClientsCount = cnt;}

    public void initializeSpace(S4RClientConf s4rClientConnInfo) {
        // Placeholder for potential initializing S4R connections
        logger.info("Initializing S4RSpace Name: " + spaceName + " Connection Config Maps Values: " + s4rClientConnInfo.getS4rConnectionMap().toString());
//        String host = s4rClientConnInfo.getS4rConnectionMap().get("host");
//        String port = s4rClientConnInfo.getS4rConnectionMap().get("port");
//        if (s4rConnFactory == null) {
//            try {
//                s4rConnFactory = new ConnectionFactory();
//                s4rConnFactory.setHost(host);
//                s4rConnFactory.setPort(Integer.parseInt(port));
//                s4rConnection = s4rConnFactory.newConnection();
//                s4rChannel = s4rConnection.createChannel();
//                s4rChannel.queueDeclare("s4r_queue1", true, false, false, null);
//                OpTimeTrackS4RClient opTimeTrackS4RClient = new OpTimeTrackS4RConsumer(
//                    this, false, 0,false,0, s4rChannel);
//                addOpTimeTrackS4RClient("1", opTimeTrackS4RClient);
//            } catch (Exception e) {
//                logger.error("Error creating new S4R Pulsar Connection: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
    }


    public boolean isShuttigDown() {
        return beingShutdown.get();
    }
    public void shutdownSpace() {
        try {
            beingShutdown.set(true);

            for (OpTimeTrackS4RClient client : opTimeTrackS4RClients.values()) {
                client.close();
            }

            // Pause 5 seconds before closing producers/consumers
            S4RAdapterUtil.pauseCurThreadExec(5);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new S4RAdapterUnexpectedException("Unexpected error when shutting down NB S4J space.");
        }
    }
}
