
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

open module virtdata.lib.random {
    requires virtdata.api;
    requires commons.math3;
    requires org.apache.logging.log4j;
    requires nb.api;
    requires virtdata.lib.basics;
    requires virtdata.annotations;

    exports io.nosqlbench.virtdata.library.random;
}
