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

package io.nosqlbench.adapter.dynamodb.optypes;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

/**
 * @see <a href="https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_GetItem.html#API_GetItem_RequestSyntax">GetItem API</a>
 * @see <a href="https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.Attributes.html">Expressions.Attributes</a>
 */
public class DDBGetItemOp extends DynamoDBOp {
    private long resultSize=0;
    private final GetItemRequest rq;

    public DDBGetItemOp(DynamoDbClient client, GetItemRequest rq) {
        super(client);
        this.rq = rq;
    }

    @Override
    public GetItemResponse apply(long value) {
        GetItemResponse rs = client.getItem(rq);
        if (rs.item()!=null) {
            resultSize = rs.item().size();
        }
        return rs;
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }
}
