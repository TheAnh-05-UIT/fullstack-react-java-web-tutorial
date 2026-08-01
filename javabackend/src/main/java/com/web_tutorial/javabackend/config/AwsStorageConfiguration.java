package com.web_tutorial.javabackend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class AwsStorageConfiguration {

    @Bean
    S3Client s3Client(StorageProperties properties) {
        properties.requiredS3Bucket();
        properties.normalizedS3Prefix();
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.requiredAwsRegion()));
        return builder.build();
    }
}
