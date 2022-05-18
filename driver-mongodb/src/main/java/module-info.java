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

import io.nosqlbench.driver.mongodb.MongoActivityType;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;

module driver.mongodb {
    requires com.codahale.metrics;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires engine.api;
    requires adapters.api;
    requires nb.api;
    requires virtdata.api;
    requires org.apache.logging.log4j;
    requires org.mongodb.bson;
    requires nb.annotations;
    provides ActivityType with MongoActivityType;
}
