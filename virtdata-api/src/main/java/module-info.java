import io.nosqlbench.virtdata.annotations.gen.DocFuncData;

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

module virtdata.api {
    exports io.nosqlbench.virtdata.core.templates;
    exports io.nosqlbench.virtdata.core.bindings;
    exports io.nosqlbench.virtdata.api.bindings;
    exports io.nosqlbench.virtdata.core.composers;
    exports io.nosqlbench.virtdata.api.templating;
    exports io.nosqlbench.virtdata.api.templating.binders;
    requires org.apache.logging.log4j;
    requires java.compiler;
    requires nb.api;
    requires org.apache.commons.lang3;
    requires com.squareup.javapoet;
    requires virtdata.lang;
    requires virtdata.annotations;
    uses DocFuncData ;
    opens io.nosqlbench.virtdata.core.templates;
    opens io.nosqlbench.virtdata.core.bindings;
}
