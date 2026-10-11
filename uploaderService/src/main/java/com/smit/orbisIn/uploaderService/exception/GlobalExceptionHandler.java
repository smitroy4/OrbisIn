package com.smit.orbisIn.uploaderService.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import software.amazon.awssdk.services.s3.model.S3Exception;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(org.springframework.web.server.ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        return error(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR, e.getReason());
    }

    @ExceptionHandler(StorageNotConfiguredException.class)
    public ResponseEntity<Map<String, Object>> notConfigured(StorageNotConfiguredException e) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<Map<String, Object>> s3(S3Exception e) {
        return error(HttpStatus.BAD_GATEWAY, "Storage error: " + e.awsErrorDetails().errorMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return new ResponseEntity<>(
                Map.of("timestamp", Instant.now().toString(), "status", status.value(), "error", message == null ? status.getReasonPhrase() : message),
                status);
    }
}
