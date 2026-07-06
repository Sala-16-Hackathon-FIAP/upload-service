package br.com.fiapx.upload.infrastructure.rest.dto;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UploadResponseTest {

    @Test
    void fromDomain_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Upload upload = new Upload(id, userId, "video.mp4", 2048L, "video/mp4",
                "s3key", "upId", UploadStatus.COMPLETED, null, now, now);

        UploadResponse response = UploadResponse.fromDomain(upload);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.originalFilename()).isEqualTo("video.mp4");
        assertThat(response.fileSize()).isEqualTo(2048L);
        assertThat(response.status()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
