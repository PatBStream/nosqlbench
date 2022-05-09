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

module driver.pulsar {
    requires engine.api;
    requires org.apache.logging.log4j;
    requires com.codahale.metrics;
    requires pulsar.client.admin.api;
    requires pulsar.client.api;
    requires adapters.api;
    requires nb.api;
    requires org.apache.commons.lang3;
    requires nb.annotations;
    requires commons.collections;
    requires org.apache.avro;
    requires pulsar.client;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.configuration2;
}
