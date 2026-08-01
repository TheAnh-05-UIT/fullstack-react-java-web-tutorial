package com.web_tutorial.javabackend.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.web_tutorial.javabackend.config.StorageProperties;
import com.web_tutorial.javabackend.config.UploadProperties;
import com.web_tutorial.javabackend.service.storage.impl.LocalObjectStorageService;
import com.web_tutorial.javabackend.service.storage.impl.S3ObjectStorageService;

import software.amazon.awssdk.services.s3.S3Client;

class StorageSelectionConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StorageTestConfiguration.class)
            .withPropertyValues(
                    "app.upload.root=./target/test-storage",
                    "app.upload.max-file-size=5MB",
                    "app.upload.max-width=4096",
                    "app.upload.max-height=4096",
                    "app.upload.max-pixels=16000000",
                    "app.upload.allowed-types=image/jpeg,image/png",
                    "app.upload.allowed-folders=general,users",
                    "app.storage.s3-prefix=uploads/images",
                    "app.storage.aws-region=ap-southeast-1");

    @Test
    void localIsTheDefaultAndS3MustBeSelectedExplicitly() {
        contextRunner.withPropertyValues("app.storage.type=local")
                .run(context -> {
                    assertThat(context).hasSingleBean(ObjectStorageService.class);
                    assertThat(context).hasSingleBean(LocalObjectStorageService.class);
                    assertThat(context).doesNotHaveBean(S3ObjectStorageService.class);
                });

        contextRunner.withPropertyValues(
                "app.storage.type=s3",
                "app.storage.s3-bucket=private-test-bucket")
                .run(context -> {
                    assertThat(context).hasSingleBean(ObjectStorageService.class);
                    assertThat(context).hasSingleBean(S3ObjectStorageService.class);
                    assertThat(context).doesNotHaveBean(LocalObjectStorageService.class);
                });
    }

    @Test
    void s3SelectionWithoutBucketFailsStartupClearly() {
        contextRunner.withPropertyValues("app.storage.type=s3")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class)
                            .rootCause()
                            .hasMessageContaining("APP_STORAGE_S3_BUCKET");
                });
    }

    @Test
    void s3ConfigurationRejectsUnresolvedBucketAndRegionPlaceholders() {
        assertMissingS3ConfigurationFails(
                "app.storage.s3-bucket=${APP_STORAGE_S3_BUCKET}",
                "app.storage.aws-region=ap-southeast-1",
                "APP_STORAGE_S3_BUCKET");
        assertMissingS3ConfigurationFails(
                "app.storage.s3-bucket=private-test-bucket",
                "app.storage.aws-region=${AWS_REGION}",
                "AWS_REGION");
    }

    private void assertMissingS3ConfigurationFails(
            String bucketProperty,
            String regionProperty,
            String expectedMessage) {
        contextRunner.withPropertyValues(
                "app.storage.type=s3",
                bucketProperty,
                regionProperty)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class)
                            .rootCause()
                            .hasMessageContaining(expectedMessage);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({UploadProperties.class, StorageProperties.class})
    @Import({LocalObjectStorageService.class, S3ObjectStorageService.class})
    static class StorageTestConfiguration {

        @Bean
        S3Client s3Client() {
            return mock(S3Client.class);
        }
    }
}
