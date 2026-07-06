package br.com.fiapx.upload.infrastructure.persistence;

import br.com.fiapx.upload.domain.model.UploadChunk;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UploadChunkEntityTest {

    @Test
    void fromDomainAndToDomain_shouldRoundTrip() {
        UUID id = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UploadChunk original = new UploadChunk(id, uploadId, 2, "etag-2", now);

        UploadChunkEntity entity = UploadChunkEntity.fromDomain(original);
        UploadChunk result = entity.toDomain();

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.uploadId()).isEqualTo(uploadId);
        assertThat(result.chunkNumber()).isEqualTo(2);
        assertThat(result.etag()).isEqualTo("etag-2");
        assertThat(result.uploadedAt()).isEqualTo(now);
    }
}
