package br.com.fiapx.upload.infrastructure.rest.dto;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UploadResponse(
    UUID id,
    UUID userId,
    String originalFilename,
    Long fileSize,
    UploadStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UploadResponse fromDomain(Upload u) {
        return new UploadResponse(u.id(), u.userId(), u.originalFilename(), u.fileSize(),
                u.status(), u.createdAt(), u.updatedAt());
    }
}
