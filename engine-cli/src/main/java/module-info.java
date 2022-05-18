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

module engine.cli {
    exports io.nosqlbench.engine.cli;
    requires engine.api;
    requires nb.api;
    requires engine.core;
    requires engine.docker;
    requires virtdata.userlibs;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires nb.annotations;
    requires adapters.api;
    uses io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
}
