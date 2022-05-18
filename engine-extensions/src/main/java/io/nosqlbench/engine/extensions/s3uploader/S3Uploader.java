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

package io.nosqlbench.engine.extensions.s3uploader;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.addins.s3.s3urlhandler.S3ClientCache;
import io.nosqlbench.addins.s3.s3urlhandler.S3UrlFields;
import io.nosqlbench.api.config.standard.NBEnvironment;
import io.nosqlbench.api.metadata.ScenarioMetadata;
import io.nosqlbench.api.metadata.ScenarioMetadataAware;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.transfer.s3.*;

import javax.script.ScriptContext;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class S3Uploader implements ScenarioMetadataAware {
    private final Logger logger;
    private final MetricRegistry metricRegistry;
    private final ScriptContext scriptContext;
    private ScenarioMetadata scenarioMetadata;

    public S3Uploader(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.scriptContext = scriptContext;
    }

    /**
     * Upload the local file path to the specified S3 URL, then return the URL of the bucket
     * in its fully expanded form. See the details on token expansions in the s3.md help docs.
     * @param localFilePath The path to the local directory
     * @param urlTemplate A template that is expanded to a valid S3 URL
     * @return The fully expanded name of the URL used for upload
     */
    public String uploadDirToUrl(String localFilePath, String urlTemplate) {
        return uploadDirToUrlTokenized(localFilePath, urlTemplate, Map.of());
    }

    /**
     * Upload the local file path to the specified S3 URL, then return the URL of the bucket
     * in its fully expanded form. See the details on token expansions in the s3.md help docs.
     * Any params which are provided supersede the normally provided values from the system.
     * @param localFilePath The path to the local directory
     * @param urlTemplate A template that is expanded to a valid S3 URL
     * @param params Additional token expansions which will take precedence over other available values.
     * @return The fully expanded name of the URL used for upload
     */
    public String uploadDirToUrlTokenized(String localFilePath, String urlTemplate, Map<String,String> params) {


        Path sourcePath = Path.of(localFilePath);
        if (!FileSystems.getDefault().equals(sourcePath.getFileSystem())) {
            throw new RuntimeException("The file must reside on the default filesystem to be uploaded by S3.");
        }
        if (!Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("path '" + sourcePath + "' is not a directory.");
        }

        Map<String,String> combined = new LinkedHashMap<>(params);
        combined.putAll(scenarioMetadata.asMap());
        String url = NBEnvironment.INSTANCE.interpolateWithTimestamp(
            urlTemplate,
                scenarioMetadata.getStartedAt(),
                combined
            )
            .orElseThrow();
        logger.debug("S3 composite URL is '" + url + "'");

        S3UrlFields fields = S3UrlFields.fromURLString(url);
        S3ClientCache s3ClientCache = new S3ClientCache();
        S3Client s3 = s3ClientCache.get(fields);

        S3TransferManager.builder().s3ClientConfiguration(S3ClientConfiguration.builder().build());
        S3ClientConfiguration.Builder builder = S3ClientConfiguration.builder();

        if (fields.accessKey!=null&&fields.secretKey!=null){
            AwsBasicCredentials credentials = AwsBasicCredentials.create(fields.accessKey, fields.secretKey);
            StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(credentials);
            builder = builder.credentialsProvider(staticCredentialsProvider);
        }
        S3ClientConfiguration s3cc = builder.build();
        S3TransferManager txManager= S3TransferManager.builder().s3ClientConfiguration(s3cc).build();

        String prefix = fields.key;

        DirectoryUpload directoryUpload = txManager.uploadDirectory(
            UploadDirectoryRequest
                .builder()
                .bucket(fields.bucket)
                .prefix(prefix)
                .sourceDirectory(sourcePath)
                .build()
        );

        try {
            directoryUpload.wait();
            CompletedDirectoryUpload result = directoryUpload.completionFuture().get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Directory upload was interrupted:" + e,e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Directory upload failed: " + e,e);
        }
        return url;
    }

    @Override
    public void setScenarioMetadata(ScenarioMetadata metadata) {
        this.scenarioMetadata = metadata;
    }
}
