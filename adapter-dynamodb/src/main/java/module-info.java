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


import io.nosqlbench.adapter.dynamodb.DynamoDBDriverAdapter;
import io.nosqlbench.adapters.api.opmapping.uniform.DriverAdapter;

module adapter.dynamodb {
    requires nb.annotations;
    requires nb.api;
    requires adapters.api;
    requires virtdata.api;
    requires nb.docsapi;
    requires software.amazon.awssdk.services.dynamodb;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.awscore;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.http.apache;
    requires org.apache.logging.log4j;
    requires software.amazon.awssdk.http;
    requires software.amazon.awssdk.core;
    provides DriverAdapter with DynamoDBDriverAdapter;
}
