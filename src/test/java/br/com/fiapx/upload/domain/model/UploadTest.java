package br.com.fiapx.upload.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UploadTest {

    @Test
    void initiate_shouldCreateUploadWithInitiatedStatus() {
        UUID userId = UUID.randomUUID();
        Upload upload = Upload.initiate(userId, "video.mp4", 1024L, "video/mp4");

        assertThat(upload.id()).isNotNull();
        assertThat(upload.userId()).isEqualTo(userId);
        assertThat(upload.originalFilename()).isEqualTo("video.mp4");
        assertThat(upload.fileSize()).isEqualTo(1024L);
        assertThat(upload.mimeType()).isEqualTo("video/mp4");
        assertThat(upload.s3Key()).contains("uploads/" + userId);
        assertThat(upload.s3Key()).contains("video.mp4");
        assertThat(upload.uploadId()).isNull();
        assertThat(upload.status()).isEqualTo(UploadStatus.INITIATED);
        assertThat(upload.errorMessage()).isNull();
        assertThat(upload.createdAt()).isNotNull();
        assertThat(upload.updatedAt()).isNotNull();
    }

    @Test
    void withUploadId_shouldSetUploadIdAndChangeStatusToUploading() {
        Upload upload = Upload.initiate(UUID.randomUUID(), "file.mp4", 500L, "video/mp4");
        Upload updated = upload.withUploadId("s3-multipart-id");

        assertThat(updated.uploadId()).isEqualTo("s3-multipart-id");
        assertThat(updated.status()).isEqualTo(UploadStatus.UPLOADING);
        assertThat(updated.id()).isEqualTo(upload.id());
        assertThat(updated.userId()).isEqualTo(upload.userId());
        assertThat(updated.originalFilename()).isEqualTo(upload.originalFilename());
    }

    @Test
    void completed_shouldChangeStatusToCompleted() {
        UUID userId = UUID.randomUUID();
        Upload upload = new Upload(UUID.randomUUID(), userId, "video.mp4", 1024L,
                "video/mp4", "key", "mid", UploadStatus.UPLOADING, null,
                LocalDateTime.now(), LocalDateTime.now());

        Upload completed = upload.completed();

        assertThat(completed.status()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(completed.errorMessage()).isNull();
        assertThat(completed.id()).isEqualTo(upload.id());
    }

    @Test
    void failed_shouldChangeStatusToFailedWithReason() {
        UUID userId = UUID.randomUUID();
        Upload upload = new Upload(UUID.randomUUID(), userId, "video.mp4", 1024L,
                "video/mp4", "key", "mid", UploadStatus.UPLOADING, null,
                LocalDateTime.now(), LocalDateTime.now());

        Upload failed = upload.failed("Processing error");

        assertThat(failed.status()).isEqualTo(UploadStatus.FAILED);
        assertThat(failed.errorMessage()).isEqualTo("Processing error");
        assertThat(failed.id()).isEqualTo(upload.id());
    }

    @Test
    void recordAccessors_shouldReturnCorrectValues() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Upload upload = new Upload(id, userId, "test.mp4", 2048L, "video/mp4",
                "s3key", "upId", UploadStatus.COMPLETED, "err", now, now);

        assertThat(upload.id()).isEqualTo(id);
        assertThat(upload.userId()).isEqualTo(userId);
        assertThat(upload.originalFilename()).isEqualTo("test.mp4");
        assertThat(upload.fileSize()).isEqualTo(2048L);
        assertThat(upload.mimeType()).isEqualTo("video/mp4");
        assertThat(upload.s3Key()).isEqualTo("s3key");
        assertThat(upload.uploadId()).isEqualTo("upId");
        assertThat(upload.status()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(upload.errorMessage()).isEqualTo("err");
        assertThat(upload.createdAt()).isEqualTo(now);
        assertThat(upload.updatedAt()).isEqualTo(now);
    }
}
