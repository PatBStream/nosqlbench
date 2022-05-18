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

import io.nosqlbench.api.activityapi.core.MetricRegistryService;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.CoreResultValueFilter;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ExperimentalResultFilterType;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.IncludeCodesTypeExperimental;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultValueFilterType;
import io.nosqlbench.engine.api.activityapi.cyclelog.inputs.cyclelog.CycleLogInputType;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogOutputType;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.logger.LoggingMarkerDispenser;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers.CodeErrorHandler;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers.*;
import io.nosqlbench.engine.api.activityapi.input.InputType;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputType;
import io.nosqlbench.engine.api.activityapi.ratelimits.HybridRateLimiter;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityapi.ratelimits.ThreadDrivenTokenPool;
import io.nosqlbench.engine.api.activityapi.ratelimits.TokenPool;
import io.nosqlbench.engine.api.activityimpl.input.TargetRateInputType;

module engine.api {
    requires nb.api;
    requires org.apache.logging.log4j;
    requires com.codahale.metrics;
    requires nb.annotations;
    requires adapters.api;
    requires org.apache.commons.text;
    requires java.scripting;
    requires annotations;
    requires jmh.core;
    requires org.graalvm.sdk;
    requires org.yaml.snakeyaml;
    requires com.github.oshi;
    requires com.google.gson;
    requires virtdata.api;
    exports io.nosqlbench.engine.api.activityapi.cyclelog.filters;
    exports io.nosqlbench.engine.api.activityapi.core;
    exports io.nosqlbench.engine.api.activityapi.errorhandling.modular;
    exports io.nosqlbench.engine.api.extensions;
    exports io.nosqlbench.engine.api.activityapi.planning;
    exports io.nosqlbench.engine.api.activityconfig;
    exports io.nosqlbench.engine.api.activityconfig.yaml;
    exports io.nosqlbench.engine.api.activityimpl;
    exports io.nosqlbench.engine.api.util;
    exports io.nosqlbench.engine.api.activityapi.ratelimits;
    exports io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets;
    exports io.nosqlbench.engine.api.activityapi.cyclelog.buffers.op_output;
    exports io.nosqlbench.engine.api.metrics;
    exports io.nosqlbench.engine.api.activityimpl.input;
    exports io.nosqlbench.engine.api.activityimpl.uniform;
    exports io.nosqlbench.engine.api.scripting;
    exports io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results;
    exports io.nosqlbench.engine.api.activityapi.input;
    exports io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog;
    exports io.nosqlbench.engine.api.activityapi.output;
    exports io.nosqlbench.engine.api.activityconfig.rawyaml;
    exports io.nosqlbench.engine.api.scenarios;
    exports io.nosqlbench.engine.api.templating;
    exports io.nosqlbench.engine.api.activityapi.errorhandling;
    exports io.nosqlbench.engine.api.activityimpl.motor;
    exports io.nosqlbench.engine.api.activityimpl.action;
    exports io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;
    provides ErrorHandler with
        WarnErrorHandler,
        CounterErrorHandler,
        CountErrorHandler,
        HistogramErrorHandler,
        IgnoreErrorHandler,
        TimerErrorHandler,
        StopErrorHandler,
        RetryErrorHandler,
        MeterErrorHandler,
        CodeErrorHandler;
    provides OutputType with CycleLogOutputType;
    provides OutputDispenser with LoggingMarkerDispenser ;
    provides InputType with TargetRateInputType, CycleLogInputType;
    provides TokenPool with ThreadDrivenTokenPool;
    provides RateLimiter with HybridRateLimiter;
    provides ResultValueFilterType with CoreResultValueFilter;
    provides ExperimentalResultFilterType with IncludeCodesTypeExperimental;
    uses ResultValueFilterType;
    uses ErrorHandler;
    uses TokenPool;
    uses MetricRegistryService;
    uses OutputType;
    uses InputType;
}
