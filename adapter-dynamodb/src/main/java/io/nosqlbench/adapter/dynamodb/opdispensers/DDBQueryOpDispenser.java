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
import io.nosqlbench.adapter.dynamodb.optypes.DDBQueryOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.opmapping.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.Map;
import java.util.function.LongFunction;

/**
 * <pre>{@code
 * {
 *    "AttributesToGet": [ "string" ],
 *    "ConditionalOperator": "string",
 *    "ConsistentRead": boolean,
 *    "ExclusiveStartKey": {
 *       "string" : {
 *          "B": blob,
 *          "BOOL": boolean,
 *          "BS": [ blob ],
 *          "L": [
 *             "AttributeValue"
 *          ],
 *          "M": {
 *             "string" : "AttributeValue"
 *          },
 *          "N": "string",
 *          "NS": [ "string" ],
 *          "NULL": boolean,
 *          "S": "string",
 *          "SS": [ "string" ]
 *       }
 *    },
 *    "ExpressionAttributeNames": {
 *       "string" : "string"
 *    },
 *    "ExpressionAttributeValues": {
 *       "string" : {
 *          "B": blob,
 *          "BOOL": boolean,
 *          "BS": [ blob ],
 *          "L": [
 *             "AttributeValue"
 *          ],
 *          "M": {
 *             "string" : "AttributeValue"
 *          },
 *          "N": "string",
 *          "NS": [ "string" ],
 *          "NULL": boolean,
 *          "S": "string",
 *          "SS": [ "string" ]
 *       }
 *    },
 *    "FilterExpression": "string",
 *    "IndexName": "string",
 *    "KeyConditionExpression": "string",
 *    "KeyConditions": {
 *       "string" : {
 *          "AttributeValueList": [
 *             {
 *                "B": blob,
 *                "BOOL": boolean,
 *                "BS": [ blob ],
 *                "L": [
 *                   "AttributeValue"
 *                ],
 *                "M": {
 *                   "string" : "AttributeValue"
 *                },
 *                "N": "string",
 *                "NS": [ "string" ],
 *                "NULL": boolean,
 *                "S": "string",
 *                "SS": [ "string" ]
 *             }
 *          ],
 *          "ComparisonOperator": "string"
 *       }
 *    },
 *    "Limit": number,
 *    "ProjectionExpression": "string",
 *    "QueryFilter": {
 *       "string" : {
 *          "AttributeValueList": [
 *             {
 *                "B": blob,
 *                "BOOL": boolean,
 *                "BS": [ blob ],
 *                "L": [
 *                   "AttributeValue"
 *                ],
 *                "M": {
 *                   "string" : "AttributeValue"
 *                },
 *                "N": "string",
 *                "NS": [ "string" ],
 *                "NULL": boolean,
 *                "S": "string",
 *                "SS": [ "string" ]
 *             }
 *          ],
 *          "ComparisonOperator": "string"
 *       }
 *    },
 *    "ReturnConsumedCapacity": "string",
 *    "ScanIndexForward": boolean,
 *    "Select": "string",
 *    "TableName": "string"
 * }
 * }</pre>
 */
public class DDBQueryOpDispenser extends BaseOpDispenser<DynamoDBOp> {

    private final LongFunction<QueryRequest> queryRqFunc;
    private final DynamoDbClient client;

    public DDBQueryOpDispenser(DynamoDbClient client, ParsedOp cmd, LongFunction<?> targetFunc) {
        super(cmd);
        this.client = client;
        this.queryRqFunc = resolveQuerySpecFunc(cmd);

        LongFunction<String> tableNameFunc = l -> targetFunc.apply(l).toString();
    }

    @Override
    public DDBQueryOp apply(long cycle) {
        QueryRequest rq = queryRqFunc.apply(cycle);
        return new DDBQueryOp(client,rq);
    }

    private LongFunction<QueryRequest> resolveQuerySpecFunc(ParsedOp cmd) {

        LongFunction<QueryRequest.Builder> qrb = l -> QueryRequest.builder();
        qrb = cmd.enhance(qrb,"projection",String.class, QueryRequest.Builder::projectionExpression);
        qrb = cmd.enhance(qrb,"ConsistentRead",boolean.class, QueryRequest.Builder::consistentRead);
        qrb = cmd.enhance(qrb,"ExclusiveStartkey",Map.class,(b,v) -> b.exclusiveStartKey(ItemValue.fromMap(v)));
        qrb = cmd.enhance(qrb,"Limit",Integer.class, QueryRequest.Builder::limit);
        LongFunction<QueryRequest.Builder> finalQrb = qrb;
        return l -> finalQrb.apply(l).build();
    }

}
