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

package io.nosqlbench.adapter.dynamodb;

import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import io.nosqlbench.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class DynamoDBSpace {
    private final static Logger logger = LogManager.getLogger(DynamoDBSpace.class);

    private final String name;
    DynamoDbClient dynamoDbClient;

    public DynamoDBSpace(String name, NBConfiguration cfg) {
        this.name = name;
        this.dynamoDbClient = createClient(cfg);
    }

    public DynamoDbClient getDynamoDbClient() {
        return dynamoDbClient;
    }

    private DynamoDbClient createClient(NBConfiguration cfg) {

        DynamoDbClientBuilder builder = DynamoDbClient.builder();
        builder = builder.defaultsMode(DefaultsMode.STANDARD);

        Optional<String> region = cfg.getOptional("region");
        Optional<String> endpoint = cfg.getOptional("endpoint");
        Optional<String> signing_region = cfg.getOptional("signing_region");
        if (region.isPresent() && (endpoint.isPresent() || signing_region.isPresent())) {
            throw new OpConfigError("If you specify region, endpoint and signing_region options are not allowed");
        }
        if (region.isPresent()) {
            builder = builder.region(Region.of(region.get()));
        } else if (endpoint.isPresent() && signing_region.isPresent()) {
            builder = builder.endpointOverride(URI.create(endpoint.get()));
        } else {
            throw new OpConfigError("Either region or endpoint and signing_region options are required.");
        }

        ApacheHttpClient.Builder cb = ApacheHttpClient.builder();

        cfg.getOptional("http_socket_timeout").or(() -> cfg.getOptional("timeout"))
            .map(Integer::parseInt).map(i -> Duration.of(i, ChronoUnit.SECONDS))
            .ifPresent(v -> {
                logger.info("http_socket_timeout=>" + v);
                cb.socketTimeout(v);
            });

        cfg.getOptional("http_connection_timeout").or(() -> cfg.getOptional("timeout"))
            .map(Integer::parseInt).map(i -> Duration.of(i, ChronoUnit.SECONDS))
            .ifPresent(v -> {
                logger.info("http_connection_timeout=>" + v);
                cb.connectionTimeout(v);
            });

        cfg.getOptional("http_connection_max_idle_time")
            .map(Integer::parseInt).map(i -> Duration.of(i, ChronoUnit.SECONDS))
            .ifPresent(v -> {
                logger.info("http_connection_max_idle_time=>" + v);
                cb.connectionMaxIdleTime(v);
            });

        cfg.getOptional("http_connection_acquisition_timeout").or(() -> cfg.getOptional("timeout"))
            .map(Integer::parseInt).map(i -> Duration.of(i, ChronoUnit.SECONDS))
            .ifPresent(v -> {
                logger.info("http_connection_acquisition_timeout=>" + v);
                cb.connectionAcquisitionTimeout(v);
            });

        cfg.getOptional("http_connection_time_to_live")
            .map(Integer::parseInt).map(i -> Duration.of(i, ChronoUnit.SECONDS))
            .ifPresent(v -> {
                logger.info("http_connection_time_to_live=>" + v);
                cb.connectionTimeToLive(v);
            });

        cfg.getOptional("http_max_connections").map(Integer::parseInt)
            .ifPresent(v -> {
                logger.info("http_max_connections=>" + v);
                cb.maxConnections(v);
            });

        cfg.getOptional("http_connection_timeout").or(() -> cfg.getOptional("timeout"))
            .map(Integer::parseInt).map(i -> Duration.of(i, ChronoUnit.SECONDS))
            .ifPresent(v -> {
                logger.info("http_connection_timeout=>" + v);
                cb.connectionTimeout(v);
            });

        SdkHttpClient sdkclient = cb.build();
        builder=builder.httpClient(sdkclient);

        return builder.build();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(DynamoDBSpace.class)
            .add(Param.optional("endpoint"))
            .add(Param.optional("signing_region"))
            .add(Param.optional("region"))

            .add(Param.optional("http_socket_timeout"))
            .add(Param.optional("http_connection_timeout"))
            .add(Param.optional("http_connection_max_idle_time"))
            .add(Param.optional("http_connection_acquisition_timeout"))
            .add(Param.optional("http_connection_time_to_live"))
            .add(Param.optional("http_max_connections"))
            .add(Param.optional("http_connection_timeout"))
            .asReadOnly();
    }

}
