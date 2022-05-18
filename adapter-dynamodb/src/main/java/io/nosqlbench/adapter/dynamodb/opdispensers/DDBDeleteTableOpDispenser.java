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

import io.nosqlbench.adapter.dynamodb.optypes.DDBDeleteTableOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.opmapping.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;

import java.util.function.LongFunction;

/**
 * <pre>{@code
 * Request Syntax
 * {
 *    "TableName": "string"
 * }
 * }</pre>
 */
public class DDBDeleteTableOpDispenser extends BaseOpDispenser<DynamoDBOp> {

    private final DynamoDbClient client;
    private final LongFunction<String> tableNameFunc;

    public DDBDeleteTableOpDispenser(DynamoDbClient client, ParsedOp cmd, LongFunction<?> targetFunc) {
        super(cmd);
        this.client = client;
        this.tableNameFunc = l -> targetFunc.apply(l).toString();
    }

    @Override
    public DDBDeleteTableOp apply(long cycle) {
        DeleteTableRequest rq = DeleteTableRequest.builder()
            .tableName(tableNameFunc.apply(cycle))
            .build();
        return new DDBDeleteTableOp(client, rq);
    }

}
