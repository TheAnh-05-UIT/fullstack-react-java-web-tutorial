package com.web_tutorial.javabackend.service.storage.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.web_tutorial.javabackend.config.StorageProperties;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.exception.UploadStorageException;
import com.web_tutorial.javabackend.service.storage.ObjectStorageService;
import com.web_tutorial.javabackend.service.storage.StorageKeyValidator;
import com.web_tutorial.javabackend.service.storage.StoredObject;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3ObjectStorageService implements ObjectStorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String prefix;

    public S3ObjectStorageService(S3Client s3Client, StorageProperties properties) {
        this.s3Client = s3Client;
        this.bucket = properties.requiredS3Bucket();
        this.prefix = properties.normalizedS3Prefix();
        properties.requiredAwsRegion();
    }

    @Override
    public void store(String key, byte[] bytes, String contentType) {
        String objectKey = objectKey(key);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(bytes));
        } catch (SdkException exception) {
            throw new UploadStorageException("Image could not be stored.", exception);
        }
    }

    @Override
    public StoredObject load(String key) {
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(objectKey(key)).build());
            return new StoredObject(response.asByteArray(), response.response().contentType());
        } catch (NoSuchKeyException exception) {
            throw new ResourceNotFoundException("Image not found.");
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new ResourceNotFoundException("Image not found.");
            }
            throw new UploadStorageException("Image could not be loaded.", exception);
        } catch (SdkException exception) {
            throw new UploadStorageException("Image could not be loaded.", exception);
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey(key))
                    .build());
        } catch (SdkException exception) {
            throw new UploadStorageException("Image could not be deleted.", exception);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey(key))
                    .build());
            return true;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return false;
            }
            throw new UploadStorageException("Image availability could not be checked.", exception);
        } catch (SdkException exception) {
            throw new UploadStorageException("Image availability could not be checked.", exception);
        }
    }

    private String objectKey(String key) {
        String managedKey = StorageKeyValidator.requireManagedKey(key);
        return prefix.isEmpty() ? managedKey : prefix + "/" + managedKey;
    }
}
