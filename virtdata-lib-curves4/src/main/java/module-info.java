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

open module virtdata.lib.curves4 {
    requires virtdata.api;
    requires virtdata.lib.basics;
    requires virtdata.annotations;
    exports io.nosqlbench.virtdata.library.curves4.continuous.common;
    exports io.nosqlbench.virtdata.library.curves4.continuous.int_double;
    exports io.nosqlbench.virtdata.library.curves4.continuous.long_double;
    exports io.nosqlbench.virtdata.library.curves4.discrete.common;
    exports io.nosqlbench.virtdata.library.curves4.discrete.int_int;
    exports io.nosqlbench.virtdata.library.curves4.discrete.int_long;
    exports io.nosqlbench.virtdata.library.curves4.discrete.long_int;
    exports io.nosqlbench.virtdata.library.curves4.discrete.long_long;
}
