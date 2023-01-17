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

import io.nosqlbench.adapter.s4r.S4RSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

abstract public class OpTimeTrackS4RClient {

    private final static Logger logger = LogManager.getLogger("OpTimeTrackS4RClient");

    protected final S4RSpace s4RSpace;

    protected final long activityStartTime;

    // Maximum time length to execute S4J operations (e.g. message send or consume)
    // - when NB execution passes this threshold, it is simply NoOp
    // - 0 means no maximum time constraint. S4JOp is always executed until NB execution cycle finishes
    protected final long maxOpTimeInSec;

    public OpTimeTrackS4RClient(S4RSpace s4RSpace) {
        this.s4RSpace = s4RSpace;
        this.activityStartTime = s4RSpace.getActivityStartTimeMills();
        this.maxOpTimeInSec = s4RSpace.getMaxOpTimeInSec();
    }

    public void process(long cycle, Object cycleObj) {

        long timeElapsedMills = System.currentTimeMillis() - activityStartTime;

        // If maximum operation duration is specified, only process messages
        // before the maximum duration threshold is reached. Otherwise, this is
        // just no-op.
        if ( (maxOpTimeInSec == 0) || (timeElapsedMills <= (maxOpTimeInSec*1000)) ) {
            cycleMsgProcess(cycle, cycleObj);
        }
    }

    abstract void cycleMsgProcess(long cycle, Object cycleObj);

    abstract public void close();
}
