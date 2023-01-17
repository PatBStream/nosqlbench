/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.nosqlbench.adapter.s4r.ops;

import com.codahale.metrics.Histogram;
import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.util.S4RAdapterMetrics;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;

public class S4ROp implements CycleOp<Object> {
    private final S4RAdapterMetrics s4rAdapterMetrics;
    protected final S4RSpace s4RSpace;
    private final OpTimeTrackS4RClient opTimeTrackS4RClient;
    private final Object cycleObj;
    protected final Histogram messageSizeHistogram;


    public S4ROp(S4RAdapterMetrics s4rAdapterMetrics,
                 S4RSpace s4RSpace,
                 OpTimeTrackS4RClient opTimeTrackS4RClient,
                 Object cycleObj)
    {
        this.s4rAdapterMetrics = s4rAdapterMetrics;
        this.s4RSpace = s4RSpace;
        this.opTimeTrackS4RClient = opTimeTrackS4RClient;
        this.cycleObj = cycleObj;
        this.messageSizeHistogram = s4rAdapterMetrics.getMessagesizeHistogram();
    }

    @Override
    public Object apply(long value) {
        opTimeTrackS4RClient.process(value, cycleObj);
        return  null;
    }
}
