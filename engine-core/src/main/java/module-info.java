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
import io.nosqlbench.api.annotations.Annotator;
import io.nosqlbench.engine.core.metrics.LoggingAnnotator;
import io.nosqlbench.engine.core.metrics.MetricsContext;

module engine.core {
    uses io.nosqlbench.adapters.api.opmapping.uniform.DriverAdapter;
    uses io.nosqlbench.engine.api.activityapi.core.ActivityType;
    uses io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
    uses Annotator;
    requires driver.diag;
    exports io.nosqlbench.engine.core.annotation;
    exports io.nosqlbench.engine.core.lifecycle;
    exports io.nosqlbench.engine.core.logging;
    exports io.nosqlbench.engine.core.metadata;
    exports io.nosqlbench.engine.core.metrics;
    exports io.nosqlbench.engine.core.script;
    requires engine.api;
    requires nb.api;
    requires org.apache.logging.log4j;
    requires nb.annotations;
    requires com.codahale.metrics;
    requires com.codahale.metrics.graphite;
    requires adapters.api;
    requires org.graalvm.sdk;
    requires org.graalvm.js.scriptengine;
    requires java.scripting;
    requires com.google.gson;
    requires hdrhistogram.metrics.reservoir;
    requires org.apache.logging.log4j.core;
    requires engine.clients;
    provides MetricRegistryService with MetricsContext;
    provides Annotator with LoggingAnnotator;
}
