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

import io.nosqlbench.adapter.dynamodb.optypes.DDBCreateTableOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.opmapping.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

/**
 * <pre>{@code
 * Request Syntax
 * {
 *    "AttributeDefinitions": [
 *       {
 *          "AttributeName": "string",
 *          "AttributeType": "string"
 *       }
 *    ],
 *    "BillingMode": "string",
 *    "GlobalSecondaryIndexes": [
 *       {
 *          "IndexName": "string",
 *          "KeySchema": [
 *             {
 *                "AttributeName": "string",
 *                "KeyType": "string"
 *             }
 *          ],
 *          "Projection": {
 *             "NonKeyAttributes": [ "string" ],
 *             "ProjectionType": "string"
 *          },
 *          "ProvisionedThroughput": {
 *             "ReadCapacityUnits": number,
 *             "WriteCapacityUnits": number
 *          }
 *       }
 *    ],
 *    "KeySchema": [
 *       {
 *          "AttributeName": "string",
 *          "KeyType": "string"
 *       }
 *    ],
 *    "LocalSecondaryIndexes": [
 *       {
 *          "IndexName": "string",
 *          "KeySchema": [
 *             {
 *                "AttributeName": "string",
 *                "KeyType": "string"
 *             }
 *          ],
 *          "Projection": {
 *             "NonKeyAttributes": [ "string" ],
 *             "ProjectionType": "string"
 *          }
 *       }
 *    ],
 *    "ProvisionedThroughput": {
 *       "ReadCapacityUnits": number,
 *       "WriteCapacityUnits": number
 *    },
 *    "SSESpecification": {
 *       "Enabled": boolean,
 *       "KMSMasterKeyId": "string",
 *       "SSEType": "string"
 *    },
 *    "StreamSpecification": {
 *       "StreamEnabled": boolean,
 *       "StreamViewType": "string"
 *    },
 *    "TableClass": "string",
 *    "TableName": "string",
 *    "Tags": [
 *       {
 *          "Key": "string",
 *          "Value": "string"
 *       }
 *    ]
 * }
 * }</pre>
 */
public class DDBCreateTableOpDispenser extends BaseOpDispenser<DynamoDBOp> {

    private final LongFunction<String> tableNameFunc;
    private final LongFunction<Collection<KeySchemaElement>> keySchemaFunc;
    private final LongFunction<Collection<AttributeDefinition>> attributeDefsFunc;
    private final LongFunction<String> readCapacityFunc;
    private final LongFunction<String> writeCapacityFunc;
    private final LongFunction<String> billingModeFunc;
    private final DynamoDbClient client;

    public DDBCreateTableOpDispenser(DynamoDbClient client, ParsedOp cmd, LongFunction<?> targetFunc) {
        super(cmd);
        this.client = client;
        this.tableNameFunc = l -> targetFunc.apply(l).toString();
        this.keySchemaFunc = resolveKeySchemaFunction(cmd);
        this.attributeDefsFunc = resolveAttributeDefinitionFunction(cmd);
        this.billingModeFunc = cmd.getAsFunctionOr("BillingMode", BillingMode.PROVISIONED.name());
        this.readCapacityFunc = cmd.getAsFunctionOr("ReadCapacityUnits", "10");
        this.writeCapacityFunc = cmd.getAsFunctionOr("WriteCapacityUnits", "10");
    }

    @Override
    public DDBCreateTableOp apply(long cycle) {

        CreateTableRequest.Builder rq = CreateTableRequest.builder();
        rq.tableName(tableNameFunc.apply(cycle));
        rq.keySchema(keySchemaFunc.apply(cycle));
        rq.attributeDefinitions(attributeDefsFunc.apply(cycle));
        BillingMode billingMode = BillingMode.valueOf(billingModeFunc.apply(cycle));
        rq.billingMode(billingMode);
        if (billingMode.equals(BillingMode.PROVISIONED)) {
            rq.provisionedThroughput(
                ProvisionedThroughput.builder()
                    .readCapacityUnits(Long.parseLong(readCapacityFunc.apply(cycle)))
                    .writeCapacityUnits(Long.parseLong(writeCapacityFunc.apply(cycle)))
                    .build()
            );
        }
        return new DDBCreateTableOp(client, rq.build());
    }

    private LongFunction<Collection<AttributeDefinition>> resolveAttributeDefinitionFunction(ParsedOp cmd) {
        LongFunction<? extends Map> attrsmap = cmd.getAsRequiredFunction("Attributes", Map.class);
        return (long l) -> {
            List<AttributeDefinition> defs = new ArrayList<>();
            attrsmap.apply(l).forEach((k, v) -> {

                AttributeDefinition ad = AttributeDefinition.builder()
                    .attributeName(k.toString())
                    .attributeType(ScalarAttributeType.valueOf(v.toString()))
                    .build();
                defs.add(ad);
            });
            return defs;
        };
    }

    private LongFunction<Collection<KeySchemaElement>> resolveKeySchemaFunction(ParsedOp cmd) {
        LongFunction<? extends Map> keysmap = cmd.getAsRequiredFunction("Keys", Map.class);

        return (long l) -> {
            List<KeySchemaElement> elems = new ArrayList<>();
            keysmap.apply(l).forEach((k, v) -> {
                KeySchemaElement ks = KeySchemaElement.builder()
                    .attributeName(k.toString())
                    .keyType(KeyType.valueOf(v.toString()))
                    .build();
                elems.add(ks);
            });
            return elems;
        };
    }


}
