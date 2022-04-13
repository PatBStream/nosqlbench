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

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.util.WeakHashMap;

/**
 * This client cache uses the credentials provided in a URL to create
 * a fingerprint, and then creates a customized S3 client for each unique
 * instance. If these clients are not used, they are allowed to be expired
 * from the map and collected.
 */
public class S3ClientCache {

    private final WeakHashMap<S3UrlFields.CredentialsFingerprint, S3Client> cache = new WeakHashMap<>();

    public S3ClientCache() {
    }

    public synchronized S3Client get(S3UrlFields fields) {
        S3Client s3 = cache.computeIfAbsent(
            fields.getCredentialsFingerprint(),
            cfp -> createAuthorizedClient(fields)
        );
        return s3;
    }

    private S3Client createAuthorizedClient(S3UrlFields fields) {

        if (fields.accessKey!=null && fields.secretKey!=null) {
            S3ClientBuilder builder = S3Client.builder();
            AwsBasicCredentials credentials = AwsBasicCredentials.create(fields.accessKey, fields.secretKey);
            StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(credentials);
            builder = builder.credentialsProvider(staticCredentialsProvider);
            return builder.build();
        } else {
            return S3Client.builder().defaultsMode(DefaultsMode.STANDARD).build();
        }
    }

}
