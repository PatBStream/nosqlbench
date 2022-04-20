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
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class DDBQueryOp extends DynamoDBOp {

    private final QueryRequest queryRq;
    private long resultSize = -1;

    public DDBQueryOp(DynamoDbClient client, QueryRequest queryRq) {
        super(client);
        this.queryRq = queryRq;
    }

    @Override
    public QueryResponse apply(long value) {
        QueryResponse rs = client.query(queryRq);
        this.resultSize = rs.count();
        return rs;
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }
}
