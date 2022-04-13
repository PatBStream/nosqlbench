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

import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.engine.extensions.csvmetrics.CSVMetricsPluginData;
import io.nosqlbench.engine.extensions.csvoutput.CsvOutputPluginData;
import io.nosqlbench.engine.extensions.example.ExamplePluginData;
import io.nosqlbench.engine.extensions.files.FileAccessPluginData;
import io.nosqlbench.engine.extensions.globalvars.GlobalVarsScriptingPluginData;
import io.nosqlbench.engine.extensions.histologger.HdrHistoLogPluginData;
import io.nosqlbench.engine.extensions.histostatslogger.HistoStatsPluginData;
import io.nosqlbench.engine.extensions.http.HttpPluginData;
import io.nosqlbench.engine.extensions.optimizers.BobyqaOptimizerPluginData;
import io.nosqlbench.engine.extensions.scriptingmetrics.ScriptingMetricsPluginData;
import io.nosqlbench.engine.shutdown.ShutdownHookPluginMetadata;

module engine.extensions {
    requires nb.annotations;
    requires engine.api;
    requires org.apache.logging.log4j;
    requires com.codahale.metrics;
    requires java.scripting;
    requires nb.api;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.transfer.s3;
    requires software.amazon.awssdk.auth;
    requires virtdata.lib.basics;
    requires commons.csv;
    requires org.graalvm.sdk;
    requires java.net.http;
    provides ScriptingPluginInfo with
        BobyqaOptimizerPluginData,
        HttpPluginData,
        ShutdownHookPluginMetadata,
        GlobalVarsScriptingPluginData,
        CSVMetricsPluginData,
        CsvOutputPluginData,
        ExamplePluginData,
        FileAccessPluginData,
        HdrHistoLogPluginData,
        HistoStatsPluginData,
        ScriptingMetricsPluginData;
}
