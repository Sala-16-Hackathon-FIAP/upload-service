package br.com.fiapx.upload.infrastructure.persistence;

import br.com.fiapx.upload.domain.model.UploadChunk;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "upload_chunks")
public class UploadChunkEntity {

    @Id
    private UUID id;

    @Column(name = "upload_id", nullable = false)
    private UUID uploadId;

    @Column(name = "chunk_number", nullable = false)
    private int chunkNumber;

    private String etag;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    protected UploadChunkEntity() {}

    public static UploadChunkEntity fromDomain(UploadChunk c) {
        UploadChunkEntity e = new UploadChunkEntity();
        e.id = c.id();
        e.uploadId = c.uploadId();
        e.chunkNumber = c.chunkNumber();
        e.etag = c.etag();
        e.uploadedAt = c.uploadedAt();
        return e;
    }

    public UploadChunk toDomain() {
        return new UploadChunk(id, uploadId, chunkNumber, etag, uploadedAt);
    }
}
