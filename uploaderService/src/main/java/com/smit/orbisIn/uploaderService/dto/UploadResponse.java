package com.smit.orbisIn.uploaderService.dto;

import java.time.Instant;

public record UploadResponse(
        String key,
        String url,
        String bucket,
        String contentType,
        long size,
        Instant uploadedAt) {
}
