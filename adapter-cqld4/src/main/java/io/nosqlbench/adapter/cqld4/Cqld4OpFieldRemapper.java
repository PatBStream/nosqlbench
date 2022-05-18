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

package io.nosqlbench.adapter.cqld4;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Cqld4OpFieldRemapper implements Supplier<List<Function<Map<String, Object>, Map<String, Object>>>> {

    @Override
    public List<Function<Map<String, Object>, Map<String, Object>>> get() {
        List<Function<Map<String, Object>, Map<String, Object>>> remappers =
            new ArrayList<Function<Map<String, Object>, Map<String, Object>>>();

        // Simplify to the modern form and provide a helpful warning to the user
        // This auto updates to 'simple: <stmt>' or 'prepared: <stmt>' for cql types
        remappers.add(m -> {
            Map<String, Object> map = new LinkedHashMap<String, Object>(m);

            if (map.containsKey("stmt")) {
                String type = map.containsKey("type") ? map.get("type").toString() : "cql";
                if (type.equals("cql")) {
                    boolean prepared = (!map.containsKey("prepared")) || map.get("prepared").equals(true);
                    map.put(prepared ? "prepared" : "simple", map.get("stmt"));
                    map.remove("stmt");
                    map.remove("type");
                }
            }
            if (map.containsKey("type")) {
                String type = map.get("type").toString();
                if (type.equals("gremlin") && map.containsKey("script")) {
                    map.put("gremlin", map.get("script").toString());
                    map.remove("script");
                    map.remove("type");
                }
                if (type.equals("gremlin") && map.containsKey("stmt")) {
                    map.put("gremlin", map.get("stmt"));
                    map.remove("type");
                    map.remove("stmt");
                }
                if (type.equals("fluent") && map.containsKey("fluent")) {
                    map.remove("type");
                }
                if (type.equals("fluent") && map.containsKey("stmt")) {
                    map.put("fluent", map.get("stmt"));
                    map.remove("stmt");
                }
            }

            return map;
        });

        return remappers;
    }
}
