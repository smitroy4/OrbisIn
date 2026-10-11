package com.smit.orbisIn.uploaderService.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.smit.orbisIn.uploaderService.dto.PresignedUploadResponse;
import com.smit.orbisIn.uploaderService.dto.UploadResponse;

import com.smit.orbisIn.uploaderService.exception.StorageNotConfiguredException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class UploaderService {

    private final ObjectProvider<S3Client> s3ClientProvider;
    private final ObjectProvider<S3Presigner> s3PresignerProvider;

    @Value("${app.storage.bucket}")
    private String bucket;

    @Value("${app.storage.public-base-url:}")
    private String publicBaseUrl;

    @Value("${app.storage.presign-expiry-minutes:15}")
    private long presignExpiryMinutes;

    @Value("${app.storage.allowed-content-types:}")
    private String allowedContentTypes;

    @Value("${app.storage.max-file-bytes:10485760}")
    private long maxFileBytes;

    private S3Client s3() {
        S3Client client = s3ClientProvider.getIfAvailable();
        if (client == null) {
            throw new StorageNotConfiguredException();
        }
        return client;
    }

    private S3Presigner presigner() {
        S3Presigner presigner = s3PresignerProvider.getIfAvailable();
        if (presigner == null) {
            throw new StorageNotConfiguredException();
        }
        return presigner;
    }

    public UploadResponse upload(MultipartFile file, String folder, Long userId) {
        validate(file);
        String key = buildKey(userId, folder, file.getOriginalFilename());
        String contentType = contentTypeOf(file);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();
        try {
            s3().putObject(putRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uploaded file", e);
        }
        return new UploadResponse(key, fileUrl(key, userId), bucket, contentType, file.getSize(), Instant.now());
    }

    /** Presigned PUT URL so browsers / other services can upload directly to storage. */
    public PresignedUploadResponse presignUpload(String filename, String contentType, String folder, Long userId) {
        String key = buildKey(userId, folder, filename);
        Duration expiry = Duration.ofMinutes(presignExpiryMinutes);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        PresignedPutObjectRequest presigned = presigner().presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(expiry)
                        .putObjectRequest(putRequest)
                        .build());
        return new PresignedUploadResponse(key, presigned.url().toString(),
                fileUrl(key, userId), expiry.toSeconds());
    }

    /** Presigned GET URL for downloading / previewing a (private) object. */
    public String presignDownload(String key, Long userId) {
        requireRead(key);
        Duration expiry = Duration.ofMinutes(presignExpiryMinutes);
        PresignedGetObjectRequest presigned = presigner().presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(expiry)
                        .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                        .build());
        return presigned.url().toString();
    }

    public void delete(String key, Long userId) {
        requireAccess(key, userId);
        s3().deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    /** Public URL if the bucket is public (or behind a CDN); otherwise falls back to endpoint/bucket/key. */
    public String fileUrl(String key, Long userId) {
        if (StringUtils.hasText(publicBaseUrl)) {
            String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
            return base + "/" + key;
        }
        return presignDownload(key, userId);
    }

    public Map<String, Object> health() {
        if (s3ClientProvider.getIfAvailable() == null) {
            return Map.of("bucket", bucket, "reachable", false, "error", "storage credentials are not configured");
        }
        try {
            s3().headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            return Map.of("bucket", bucket, "reachable", true);
        } catch (NoSuchBucketException e) {
            return Map.of("bucket", bucket, "reachable", false, "error", "bucket does not exist");
        } catch (Exception e) {
            return Map.of("bucket", bucket, "reachable", false, "error", e.getMessage());
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > maxFileBytes) {
            throw new IllegalArgumentException("File exceeds max size of " + maxFileBytes + " bytes");
        }
        if (StringUtils.hasText(allowedContentTypes)) {
            String type = contentTypeOf(file);
            boolean allowed = false;
            for (String a : allowedContentTypes.split(",")) {
                if (type.equalsIgnoreCase(a.trim())) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new IllegalArgumentException("Content type not allowed: " + type);
            }
        }
    }

    private String contentTypeOf(MultipartFile file) {
        return StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
    }

    private String buildKey(Long userId, String folder, String filename) {
        String clean = StringUtils.hasText(filename) ? StringUtils.cleanPath(filename) : "file";
        clean = clean.replaceAll("[^a-zA-Z0-9._-]", "_");
        String prefix = StringUtils.hasText(folder)
                ? folder.replaceAll("[^a-zA-Z0-9/_-]", "").replaceAll("^/+|/+$", "") + "/"
                : "";
        // Namespace every object under the owning user so users can never
        // overwrite, read, or delete each other's files by guessing keys.
        return "u" + userId + "/" + prefix + Instant.now().getEpochSecond() + "-" + UUID.randomUUID().toString().substring(0, 8) + "-" + clean;
    }

    /**
     * New keys live under {@code u{userId}/}. Keys uploaded before namespacing
     * ({@code uploads/...}) stay accessible to any authenticated user so old
     * data keeps working; everything else is private to its owner.
     */
    private void requireAccess(String key, Long userId) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Key must not be empty");
        }
        String owned = "u" + userId + "/";
        if (key.startsWith(owned) || key.startsWith("uploads/")) {
            return;
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "You do not have access to this file");
    }

    /**
     * Reads are open to any authenticated user: post images must be viewable
     * by everyone who can see the post, even though only the owner (or legacy
     * shared keys) can delete them.
     */
    private void requireRead(String key) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Key must not be empty");
        }
        if (key.matches("u\\d+/.*") || key.startsWith("uploads/")) {
            return;
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "You do not have access to this file");
    }
}
