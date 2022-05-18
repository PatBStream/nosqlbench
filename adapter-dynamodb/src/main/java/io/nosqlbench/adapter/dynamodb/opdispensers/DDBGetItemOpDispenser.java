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

package io.nosqlbench.adapter.dynamodb.opdispensers;

import io.nosqlbench.adapter.dynamodb.converters.ItemValue;
import io.nosqlbench.adapter.dynamodb.optypes.DDBGetItemOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.opmapping.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.Map;
import java.util.function.LongFunction;

public class DDBGetItemOpDispenser extends BaseOpDispenser<DynamoDBOp> {
    private final DynamoDbClient client;
    private final LongFunction<GetItemRequest> getItemRequestFunc;

    public DDBGetItemOpDispenser(DynamoDbClient client, ParsedOp cmd, LongFunction<?> targetFunction) {
        super(cmd);
        this.client = client;
        this.getItemRequestFunc = resolveRequestFunc(cmd);

    }

    private LongFunction<GetItemRequest> resolveRequestFunc(ParsedOp cmd) {
        LongFunction<GetItemRequest.Builder> buildfunc = l -> GetItemRequest.builder();
        buildfunc = cmd.enhance(buildfunc, "table", String.class, GetItemRequest.Builder::tableName);
        buildfunc = cmd.enhance(buildfunc,"key",Map.class,(b, v) -> b.key(ItemValue.fromMap(v)));
        buildfunc = cmd.enhance(buildfunc, "projection", String.class, GetItemRequest.Builder::projectionExpression);
        buildfunc = cmd.enhance(buildfunc, "ConsistentRead",Boolean.class, GetItemRequest.Builder::consistentRead);
        LongFunction<GetItemRequest.Builder> finalBuildfunc = buildfunc;
        return l -> finalBuildfunc.apply(l).build();
    }

    @Override
    public DDBGetItemOp apply(long value) {
        return new DDBGetItemOp(client, this.getItemRequestFunc.apply(value));
    }
}
