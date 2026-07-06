package br.com.fiapx.upload.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UploadChunkTest {

    @Test
    void create_shouldGenerateIdAndTimestamp() {
        UUID uploadId = UUID.randomUUID();
        UploadChunk chunk = UploadChunk.create(uploadId, 1, "etag-1");

        assertThat(chunk.id()).isNotNull();
        assertThat(chunk.uploadId()).isEqualTo(uploadId);
        assertThat(chunk.chunkNumber()).isEqualTo(1);
        assertThat(chunk.etag()).isEqualTo("etag-1");
        assertThat(chunk.uploadedAt()).isNotNull();
    }

    @Test
    void recordAccessors_shouldReturnCorrectValues() {
        UUID id = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UploadChunk chunk = new UploadChunk(id, uploadId, 3, "etag-3", now);

        assertThat(chunk.id()).isEqualTo(id);
        assertThat(chunk.uploadId()).isEqualTo(uploadId);
        assertThat(chunk.chunkNumber()).isEqualTo(3);
        assertThat(chunk.etag()).isEqualTo("etag-3");
        assertThat(chunk.uploadedAt()).isEqualTo(now);
    }
}
