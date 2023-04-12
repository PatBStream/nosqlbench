package io.nosqlbench.adapter.s4r.dispensers;

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


import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.ops.S4ROp;
import io.nosqlbench.adapter.s4r.util.S4RAdapterUtil;
import io.nosqlbench.adapter.s4r.util.S4RAdapterMetrics;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterInvalidParamException;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;
import java.util.function.Predicate;

public abstract  class S4RBaseOpDispenser extends BaseOpDispenser<S4ROp, S4RSpace> {

    private final static Logger logger = LogManager.getLogger("S4RBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final S4RAdapterMetrics s4rAdapterMetrics;
    protected final S4RSpace s4RSpace;

    protected final int s4rClntCnt;
    protected final int consumerGrpCnt;

    // Doc-level parameter: async_api (default: true)
    // - For Producer workload, this means waiting for message send ack. synchronously or asynchronously
    // - For Consumer workload, this means doing manual message commit synchronously or asynchronously
    //   Only relevant when auto.commit is disabled
    protected final boolean asyncAPI;

    protected final LongFunction<String> topicNameStrFunc;
    protected final Map<String, String> topicConfMap = new HashMap<>();

    protected final int totalThreadNum;
    protected final long totalCycleNum;

    public S4RBaseOpDispenser(DriverAdapter adapter,
                              ParsedOp op,
                              LongFunction<String> topicNameStrFunc,
                              S4RSpace s4RSpace) {

        super(adapter, op);

        this.parsedOp = op;
        this.s4RSpace = s4RSpace;

        String defaultMetricsPrefix = getDefaultMetricsPrefix(this.parsedOp);
        this.s4rAdapterMetrics = new S4RAdapterMetrics(defaultMetricsPrefix);
        s4rAdapterMetrics.initS4RAdapterInstrumentation();

        this.asyncAPI =
            parsedOp.getStaticConfigOr(S4RAdapterUtil.DOC_LEVEL_PARAMS.ASYNC_API.label, Boolean.TRUE);

        this.topicNameStrFunc = topicNameStrFunc;
        this.topicConfMap.putAll(s4RSpace.getS4RClientConf().getTopicConfMap());

        this.totalCycleNum = NumberUtils.toLong(parsedOp.getStaticConfig("cycles", String.class));
        s4RSpace.setTotalCycleNum(totalCycleNum);

        this.s4rClntCnt = s4RSpace.getS4RClntNum();
        this.consumerGrpCnt = s4RSpace.getConsumerGrpNum();
        this.totalThreadNum = NumberUtils.toInt(parsedOp.getStaticConfig("threads", String.class));

        assert (s4rClntCnt > 0);
        assert (consumerGrpCnt > 0);

        boolean validThreadNum =
            ( ((this instanceof MessageProducerOpDispenser) && (totalThreadNum == s4rClntCnt)) ||
                ((this instanceof MessageConsumerOpDispenser) && (totalThreadNum == s4rClntCnt*consumerGrpCnt)) );
        if (!validThreadNum) {
            throw new S4RAdapterInvalidParamException(
                "Incorrect settings of 'threads', 'num_clnt', or 'num_cons_grp' -- "  +
                    totalThreadNum + ", " + s4rClntCnt + ", " + consumerGrpCnt);
        }
    }

    public S4RSpace getS4RSpace() { return s4RSpace; }
    public S4RAdapterMetrics getS4RAdapterMetrics() { return s4rAdapterMetrics; }

    protected LongFunction<Boolean> lookupStaticBoolConfigValueFunc(String paramName, boolean defaultValue) {
        LongFunction<Boolean> booleanLongFunction;
        booleanLongFunction = (l) -> parsedOp.getOptionalStaticConfig(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> BooleanUtils.toBoolean(value))
            .orElse(defaultValue);
        logger.info("{}: {}", paramName, booleanLongFunction.apply(0));
        return  booleanLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName, String defaultValue) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse((l) -> defaultValue);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName) {
        return lookupOptionalStrOpValueFunc(paramName, "");
    }

    // Mandatory Op parameter. Throw an error if not specified or having empty value
    protected LongFunction<String> lookupMandtoryStrOpValueFunc(String paramName) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsRequiredFunction(paramName, String.class);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
}
