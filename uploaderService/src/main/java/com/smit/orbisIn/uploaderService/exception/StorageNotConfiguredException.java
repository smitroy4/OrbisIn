package com.smit.orbisIn.uploaderService.exception;

public class StorageNotConfiguredException extends RuntimeException {
    public StorageNotConfiguredException() {
        super("Object storage is not configured. Set AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY (and AWS_ENDPOINT_URL_S3 for S3-compatible providers) and restart the service.");
    }
}
