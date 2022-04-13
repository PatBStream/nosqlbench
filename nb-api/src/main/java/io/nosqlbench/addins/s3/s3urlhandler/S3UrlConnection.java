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

package io.nosqlbench.addins.s3.s3urlhandler;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class S3UrlConnection extends URLConnection {

    private final S3ClientCache clientCache;

    protected S3UrlConnection(S3ClientCache clientCache, URL url) {
        super(url);
        this.clientCache = clientCache;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        S3UrlFields fields = new S3UrlFields(url);
        S3Client s3 = clientCache.get(fields);
        GetObjectRequest request = GetObjectRequest.builder().bucket(fields.bucket).key(fields.key).build();
        ResponseInputStream<GetObjectResponse> objectStream = s3.getObject(request);
        return objectStream;
    }

    @Override
    public void connect() throws IOException {
    }
}
