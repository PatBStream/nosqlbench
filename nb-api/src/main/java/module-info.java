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

import java.net.spi.URLStreamHandlerProvider;

module nb.api {
    provides URLStreamHandlerProvider with io.nosqlbench.addins.s3.s3urlhandler.S3UrlStreamHandlerProvider;
    uses MetricRegistryService;
    requires transitive nb.annotations;
    requires transitive com.google.gson;

    exports io.nosqlbench.api.activityimpl;
    exports io.nosqlbench.api.content;
    exports io.nosqlbench.api.errors;
    exports io.nosqlbench.api.metrics;
    exports io.nosqlbench.api.config.standard;
    exports io.nosqlbench.api.config.params;
    exports io.nosqlbench.api.config.fieldreaders;
    exports io.nosqlbench.api.spi;
    exports io.nosqlbench.api.metadata;
    exports io.nosqlbench.api.annotations;
    exports io.nosqlbench.addins.s3.s3urlhandler;
    exports io.nosqlbench.api.activityapi.core;
    exports io.nosqlbench.api.logging;
    exports io.nosqlbench.api.testutils;
    exports io.nosqlbench.api.ssl;
    requires org.graalvm.sdk;
    requires java.scripting;
    requires com.codahale.metrics;
    requires HdrHistogram;
    requires org.apache.commons.text;
    requires org.apache.commons.lang3;
    requires org.apache.logging.log4j;
    requires commons.csv;
    requires ascii.data;
    requires com.github.oshi;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.awscore;
    requires software.amazon.awssdk.core;
//    exports io.nosqlbench.api.servicetypes;
//    uses ResultValueFilterType;
}
