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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class OpAsJsonStmtRemapper extends OptionalStringFieldRemapper {

    public OpAsJsonStmtRemapper() {
        super("stmt", FIELDFUNC);
    }

    public static Function<String, Optional<Map<String,Object>>> FIELDFUNC = s -> {
        if (s.startsWith("{")) {
            JsonElement element = JsonParser.parseString(s);
            if (element.isJsonObject()) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Map<?,?> map = gson.fromJson(s, Map.class);
                LinkedHashMap<String, Object> newmap = new LinkedHashMap<>();
                map.forEach((k,v)->newmap.put(k.toString(),v));
                return Optional.of(newmap);
            } else {
                throw new RuntimeException("value for string op appears to be JSON, but doesn't parse to an object.");
            }
        } else {
            return Optional.empty();
        }
    };
}
