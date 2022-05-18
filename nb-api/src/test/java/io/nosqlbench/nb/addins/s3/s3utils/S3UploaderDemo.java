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

package io.nosqlbench.nb.addins.s3.s3utils;

import io.nosqlbench.addins.s3.s3urlhandler.S3ClientCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.transfer.s3.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.UploadDirectoryRequest;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

/**
 * This is a generic s3 directory uploader which is neither a scripting plugin nor a standard URL handler.
 */
public class S3UploaderDemo {

    private final S3ClientCache clientCache = new S3ClientCache();

    private static final Logger logger = LogManager.getLogger(S3UploaderDemo.class);

    public CompletedDirectoryUpload syncup(Path sourcePath, String bucket, String prefix) {

        if (!FileSystems.getDefault().equals(sourcePath.getFileSystem())) {
            throw new RuntimeException("The file must reside on the default filesystem to be uploaded by S3.");
        }

        if (!Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("path '" + sourcePath + "' is not a directory.");
        }

        S3TransferManager txManager= S3TransferManager.builder().build();
        DirectoryUpload directoryUpload = txManager.uploadDirectory(
            UploadDirectoryRequest
                .builder()
                .bucket(bucket)
                .prefix(prefix)
                .sourceDirectory(sourcePath)
                .build()
        );

        try {
            directoryUpload.wait();
            return directoryUpload.completionFuture().get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Directory upload was interrupted!");
        } catch (ExecutionException e) {
            throw new RuntimeException("Directory upload failed: " + e, e);
        }

    }

}
