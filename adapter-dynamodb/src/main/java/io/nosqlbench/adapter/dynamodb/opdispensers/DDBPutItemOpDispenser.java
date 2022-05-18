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
import io.nosqlbench.adapter.dynamodb.optypes.DDBPutItemOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.opmapping.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.api.errors.OpConfigError;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.function.LongFunction;

public class DDBPutItemOpDispenser extends BaseOpDispenser<DynamoDBOp> {

    private final DynamoDbClient client;
    private final LongFunction<String> tableNameFunc;
    private final LongFunction<? extends ItemValue> itemfunc;

    public DDBPutItemOpDispenser(DynamoDbClient client, ParsedOp cmd, LongFunction<?> targetFunc) {
        super(cmd);
        this.client = client;
        this.tableNameFunc = l -> targetFunc.apply(l).toString();
        if (cmd.isDefined("item")) {
            LongFunction<? extends Map> f1 = cmd.getAsRequiredFunction("item", Map.class);
            this.itemfunc = l -> ItemValue.fromMap(f1.apply(l));
        } else if (cmd.isDefined("json")) {
            LongFunction<? extends String> f1 = cmd.getAsRequiredFunction("json", String.class);
            this.itemfunc = l -> ItemValue.fromJSON(f1.apply(l));
        } else {
            throw new OpConfigError("PutItem op templates require either an 'item' map field or a 'json' text field");
        }
    }

    @Override
    public DynamoDBOp apply(long value) {
        String tablename = tableNameFunc.apply(value);
        ItemValue itemval = itemfunc.apply(value);
        PutItemRequest rq = PutItemRequest.builder()
            .item(itemval)
            .tableName(tablename)
            .build();
        return new DDBPutItemOp(client,rq);
    }
}
