package io.nosqlbench.driver.jms.ops;

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

/**
 * Base type of all Sync Pulsar Operations including Producers and Consumers.
 */
public abstract class S4JTimeTrackOp implements S4JOp {

    protected final long curNBCycleNum;
    protected final long s4jOpStartTimeMills;
    protected final long maxS4jOpDurationInSec;

    public S4JTimeTrackOp(long curCycleNum, long s4jOpStartTimeMills, long maxS4jOpDurationInSec) {
        this.curNBCycleNum = curCycleNum;
        this.s4jOpStartTimeMills = s4jOpStartTimeMills;
        this.maxS4jOpDurationInSec = maxS4jOpDurationInSec;
        assert (this.maxS4jOpDurationInSec >= 0);
    }

    public void run(Runnable timeTracker) {
        try {
            this.run();
        } finally {
            timeTracker.run();
        }
    }

    public abstract void run();
}
