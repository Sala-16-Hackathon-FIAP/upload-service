package br.com.fiapx.upload.infrastructure.persistence;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uploads")
public class UploadEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "upload_id")
    private String uploadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UploadEntity() {}

    public static UploadEntity fromDomain(Upload u) {
        UploadEntity e = new UploadEntity();
        e.id = u.id();
        e.userId = u.userId();
        e.originalFilename = u.originalFilename();
        e.fileSize = u.fileSize();
        e.mimeType = u.mimeType();
        e.s3Key = u.s3Key();
        e.uploadId = u.uploadId();
        e.status = u.status();
        e.errorMessage = u.errorMessage();
        e.createdAt = u.createdAt();
        e.updatedAt = u.updatedAt();
        return e;
    }

    public Upload toDomain() {
        return new Upload(id, userId, originalFilename, fileSize, mimeType, s3Key, uploadId, status, errorMessage, createdAt, updatedAt);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
}
