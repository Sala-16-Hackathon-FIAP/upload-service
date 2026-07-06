package br.com.fiapx.upload.infrastructure.persistence;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UploadEntityTest {

    @Test
    void fromDomainAndToDomain_shouldRoundTrip() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Upload original = new Upload(id, userId, "video.mp4", 1024L, "video/mp4",
                "s3key", "uploadId", UploadStatus.UPLOADING, "some error", now, now);

        UploadEntity entity = UploadEntity.fromDomain(original);
        Upload result = entity.toDomain();

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.originalFilename()).isEqualTo("video.mp4");
        assertThat(result.fileSize()).isEqualTo(1024L);
        assertThat(result.mimeType()).isEqualTo("video/mp4");
        assertThat(result.s3Key()).isEqualTo("s3key");
        assertThat(result.uploadId()).isEqualTo("uploadId");
        assertThat(result.status()).isEqualTo(UploadStatus.UPLOADING);
        assertThat(result.errorMessage()).isEqualTo("some error");
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void getters_shouldReturnCorrectValues() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Upload upload = new Upload(id, userId, "f.mp4", 100L, "video/mp4",
                "k", "u", UploadStatus.COMPLETED, null,
                LocalDateTime.now(), LocalDateTime.now());

        UploadEntity entity = UploadEntity.fromDomain(upload);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
    }
}
