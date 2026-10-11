package com.smit.orbisIn.uploaderService.dto;

public record PresignedUploadResponse(
        String key,
        String uploadUrl,
        String fileUrl,
        long expiresInSeconds) {
}
