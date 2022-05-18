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

module engine.rest {
    requires com.fasterxml.jackson.annotation;
    requires org.joda.time;
    requires com.google.gson;
    requires nb.api;
    requires swagger.parser;
    requires io.swagger.v3.oas.models;
    requires swagger.parser.core;
    requires org.yaml.snakeyaml;
    requires engine.cli;
    requires engine.core;
    requires nb.annotations;
    requires jakarta.inject;
    requires org.apache.logging.log4j;
    requires engine.api;
    requires docsys;
    requires jetty.servlet.api;
}
