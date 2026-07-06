package br.com.fiapx.upload.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record UploadChunk(
    UUID id,
    UUID uploadId,
    int chunkNumber,
    String etag,
    LocalDateTime uploadedAt
) {
    public static UploadChunk create(UUID uploadId, int chunkNumber, String etag) {
        return new UploadChunk(UUID.randomUUID(), uploadId, chunkNumber, etag, LocalDateTime.now());
    }
}
