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

import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapters.api.opmapping.uniform.DriverAdapter;

module adapter.cqld4 {
    requires com.datastax.oss.driver.core;
    requires nb.annotations;
    requires nb.docsapi;
    requires adapters.api;
    requires nb.api;
    requires org.apache.logging.log4j;
    requires com.google.gson;
    requires virtdata.api;
    requires virtdata.annotations;
    requires gremlin.core;
    requires org.codehaus.groovy;
    requires snappy.java;
    requires virtdata.lib.curves4;
    requires com.datastax.oss.protocol;
    provides DriverAdapter with Cqld4DriverAdapter;
}
