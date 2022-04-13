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

module nb.docsapi {
    exports io.nosqlbench.docsapi;
    exports io.nosqlbench.nb.api.markdown;
    requires java.compiler;
    requires nb.annotations;
    requires flexmark;
    requires flexmark.ext.yaml.front.matter;
    requires flexmark.util.ast;
    requires org.apache.logging.log4j;
    requires annotations;
    requires org.yaml.snakeyaml;
    requires flexmark.html2md.converter;
    requires flexmark.util.sequence;
//    provides Processor  with PackageDocsProcessor;
}
