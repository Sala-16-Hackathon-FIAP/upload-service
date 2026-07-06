package br.com.fiapx.upload.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Upload(
    UUID id,
    UUID userId,
    String originalFilename,
    Long fileSize,
    String mimeType,
    String s3Key,
    String uploadId,
    UploadStatus status,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static Upload initiate(UUID userId, String filename, Long fileSize, String mimeType) {
        UUID id = UUID.randomUUID();
        String s3Key = "uploads/" + userId + "/" + id + "/" + filename;
        return new Upload(id, userId, filename, fileSize, mimeType, s3Key, null,
                UploadStatus.INITIATED, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public Upload withUploadId(String uploadId) {
        return new Upload(id, userId, originalFilename, fileSize, mimeType, s3Key,
                uploadId, UploadStatus.UPLOADING, errorMessage, createdAt, LocalDateTime.now());
    }

    public Upload completed() {
        return new Upload(id, userId, originalFilename, fileSize, mimeType, s3Key,
                uploadId, UploadStatus.COMPLETED, null, createdAt, LocalDateTime.now());
    }

    public Upload failed(String reason) {
        return new Upload(id, userId, originalFilename, fileSize, mimeType, s3Key,
                uploadId, UploadStatus.FAILED, reason, createdAt, LocalDateTime.now());
    }
}
