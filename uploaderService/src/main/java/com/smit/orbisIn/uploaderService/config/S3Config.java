package com.smit.orbisIn.uploaderService.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.S3Presigner.Builder;

@Configuration
public class S3Config {

    @Value("${app.storage.endpoint:}")
    private String endpoint;

    @Value("${app.storage.region}")
    private String region;

    @Value("${app.storage.access-key:}")
    private String accessKey;

    @Value("${app.storage.secret-key:}")
    private String secretKey;

    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
    }

    private S3Configuration pathStyle() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    private S3ClientBuilder applyEndpoint(S3ClientBuilder builder) {
        // S3-compatible providers (Neon) need an explicit endpoint override.
        // Empty means real AWS: use the SDK default for the region.
        if (StringUtils.hasText(endpoint)) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        return builder;
    }

    private Builder applyEndpoint(Builder builder) {
        if (StringUtils.hasText(endpoint)) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        return builder;
    }

    @Bean
    @Conditional(StorageCondition.class)
    public S3Client s3Client() {
        S3ClientBuilder builder = applyEndpoint(S3Client.builder())
                .region(Region.of(region))
                // S3-compatible providers (Neon) require path-style access
                .serviceConfiguration(pathStyle())
                .credentialsProvider(credentialsProvider());
        builder.overrideConfiguration(b -> b.apiCallTimeout(Duration.ofSeconds(60)));
        return builder.build();
    }

    @Bean
    @Conditional(StorageCondition.class)
    public S3Presigner s3Presigner() {
        Builder builder = applyEndpoint(S3Presigner.builder())
                .region(Region.of(region))
                .serviceConfiguration(pathStyle())
                .credentialsProvider(credentialsProvider());
        return builder.build();
    }
}
