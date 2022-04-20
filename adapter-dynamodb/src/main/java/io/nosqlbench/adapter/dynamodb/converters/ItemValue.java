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

package io.nosqlbench.adapter.dynamodb.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemValue extends HashMap<String, AttributeValue> implements Map<String, AttributeValue> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ItemValue(int size) {
        super(size);
    }

    public static ItemValue fromMap(Map<? extends String,?> itemmap) {
        ItemValue iv = new ItemValue(itemmap.size());
        itemmap.forEach((k,v) -> iv.put(k,from(v)));
        return iv;
    }

    public static AttributeValue from(Object v) {
        return switch (v) {
            case String value -> AttributeValue.fromS(value);
            case Number value -> AttributeValue.fromN(String.valueOf(value));
            case Boolean value -> AttributeValue.fromBool(value);
            case SdkBytes sdkBytes -> AttributeValue.fromB(sdkBytes);
            case ByteBuffer bb -> AttributeValue.fromB(SdkBytes.fromByteArray(bb.array()));
            case byte[] bytes -> AttributeValue.fromB(SdkBytes.fromByteArray(bytes));
            case Map value -> AttributeValue.fromM(new HashMap(value) {{ replaceAll((mapkey,mapval)-> from(mapval)); }});
            case List value -> AttributeValue.fromL(new ArrayList(value) {{ replaceAll(listval -> from(listval)); }});
            case null, default -> throw new RuntimeException("Unable to map unknown type '" + v.getClass() + "' to a known DynamoDB Attribute type.");
        };
    }

    public static ItemValue fromJSON(String json) {
        return fromMap(gson.fromJson(json,Map.class));
    }

}
