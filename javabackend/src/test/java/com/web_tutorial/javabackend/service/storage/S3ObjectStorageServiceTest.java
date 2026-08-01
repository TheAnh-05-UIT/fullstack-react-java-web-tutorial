package com.web_tutorial.javabackend.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.web_tutorial.javabackend.config.StorageProperties;
import com.web_tutorial.javabackend.config.StorageProperties.Type;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.service.storage.impl.S3ObjectStorageService;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

class S3ObjectStorageServiceTest {

    private static final String MANAGED_KEY =
            "users/550e8400-e29b-41d4-a716-446655440000.png";

    private S3Client s3Client;
    private S3ObjectStorageService storage;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        storage = new S3ObjectStorageService(
                s3Client,
                new StorageProperties(Type.S3, "private-upload-bucket", "uploads/images", "ap-southeast-1"));
    }

    @Test
    void storeUsesPrivateServerGeneratedObjectKeyAndContentMetadata() {
        byte[] bytes = "image".getBytes(StandardCharsets.UTF_8);

        storage.store(MANAGED_KEY, bytes, "image/png");

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().bucket()).isEqualTo("private-upload-bucket");
        assertThat(request.getValue().key())
                .isEqualTo("uploads/images/" + MANAGED_KEY);
        assertThat(request.getValue().contentType()).isEqualTo("image/png");
        assertThat(request.getValue().contentLength()).isEqualTo(bytes.length);
        assertThat(request.getValue().acl()).isNull();
    }

    @Test
    void loadDeleteAndExistsUseTheSameManagedKey() {
        byte[] bytes = "image".getBytes(StandardCharsets.UTF_8);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(
                ResponseBytes.fromByteArray(
                        GetObjectResponse.builder().contentType("image/png").build(),
                        bytes));
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        StoredObject loaded = storage.load(MANAGED_KEY);
        storage.delete(MANAGED_KEY);

        assertThat(loaded.bytes()).isEqualTo(bytes);
        assertThat(loaded.contentType()).isEqualTo("image/png");
        assertThat(storage.exists(MANAGED_KEY)).isTrue();
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void missingObjectsAreReportedWithoutLeakingS3Details() {
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(
                S3Exception.builder().statusCode(404).message("private detail").build());

        assertThatThrownBy(() -> storage.load(MANAGED_KEY))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Image not found.");
    }

    @Test
    void s3ModeRequiresAnExplicitBucket() {
        assertThatThrownBy(() -> new S3ObjectStorageService(
                s3Client,
                new StorageProperties(Type.S3, " ", "uploads/images", "ap-southeast-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_STORAGE_S3_BUCKET");
    }
}
