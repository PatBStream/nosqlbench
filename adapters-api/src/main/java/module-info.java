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
module adapters.api {
    requires com.codahale.metrics;
    requires nb.api;
    requires com.google.gson;
    requires virtdata.api;
    requires org.apache.logging.log4j;
    requires nb.annotations;
    exports io.nosqlbench.adapters.api.templating;
    exports io.nosqlbench.adapters.api.opmapping;
    exports io.nosqlbench.adapters.api.opmapping.uniform.flowtypes;
    exports io.nosqlbench.adapters.api.activityconfig.yaml;
    exports io.nosqlbench.adapters.api.opmapping.uniform;
    opens io.nosqlbench.adapters.api.templating;
}
