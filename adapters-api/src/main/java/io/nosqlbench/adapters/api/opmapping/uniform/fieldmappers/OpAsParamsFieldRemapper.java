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

package io.nosqlbench.adapters.api.opmapping.uniform.fieldmappers;

import io.nosqlbench.api.config.params.ParamsParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class OpAsParamsFieldRemapper extends OptionalStringFieldRemapper {


    public OpAsParamsFieldRemapper() {
        super("stmt", FIELDFUNC);
    }

    public static Function<String, Optional<Map<String,Object>>> FIELDFUNC = s -> {
        if (ParamsParser.hasValues(s)) {
            Map<String, String> values = ParamsParser.parse(s, false);
            return Optional.of(new LinkedHashMap<String,Object>(values));
        } else {
            return Optional.empty();
        }
    };
}
