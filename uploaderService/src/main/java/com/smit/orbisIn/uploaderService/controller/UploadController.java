package com.smit.orbisIn.uploaderService.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.smit.orbisIn.uploaderService.context.ContextHolder;
import com.smit.orbisIn.uploaderService.dto.PresignedUploadResponse;
import com.smit.orbisIn.uploaderService.dto.UploadResponse;
import com.smit.orbisIn.uploaderService.service.UploaderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
public class UploadController {

    private final UploaderService uploaderService;

    private Long currentUserId() {
        Long userId = ContextHolder.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid user context");
        }
        return userId;
    }

    /** Server-side upload: POST /uploads/core/upload (multipart form field "file"). */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "uploads") String folder) {
        return new ResponseEntity<>(uploaderService.upload(file, folder, currentUserId()), HttpStatus.CREATED);
    }

    /** Presigned direct upload: GET /uploads/core/presign-upload?filename=x.png&contentType=image/png */
    @GetMapping("/presign-upload")
    public ResponseEntity<PresignedUploadResponse> presignUpload(
            @RequestParam String filename,
            @RequestParam(required = false, defaultValue = "application/octet-stream") String contentType,
            @RequestParam(required = false, defaultValue = "uploads") String folder) {
        return ResponseEntity.ok(uploaderService.presignUpload(filename, contentType, folder, currentUserId()));
    }

    /** Presigned download/preview URL: GET /uploads/core/presign-download?key=... */
    @GetMapping("/presign-download")
    public ResponseEntity<Map<String, String>> presignDownload(@RequestParam String key) {
        return ResponseEntity.ok(Map.of("url", uploaderService.presignDownload(key, currentUserId())));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String key) {
        uploaderService.delete(key, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health/storage")
    public ResponseEntity<Map<String, Object>> storageHealth() {
        return ResponseEntity.ok(uploaderService.health());
    }
}
