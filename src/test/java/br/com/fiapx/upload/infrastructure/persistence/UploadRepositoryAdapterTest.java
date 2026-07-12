package br.com.fiapx.upload.infrastructure.persistence;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadRepositoryAdapterTest {

    @Mock private UploadJpaRepository uploadJpa;
    @Mock private UploadChunkJpaRepository chunkJpa;

    @InjectMocks
    private UploadRepositoryAdapter adapter;

    @Test
    void markProcessingStarted_delegatesToJpa() {
        UUID id = UUID.randomUUID();
        LocalDateTime ts = LocalDateTime.now();
        adapter.markProcessingStarted(id, ts);
        verify(uploadJpa).markProcessingStarted(id, ts);
    }

    @Test
    void incrementReconciliationAttempts_delegatesToJpa() {
        UUID id = UUID.randomUUID();
        adapter.incrementReconciliationAttempts(id);
        verify(uploadJpa).incrementReconciliationAttempts(id);
    }

    @Test
    void findStaleCompletedWithoutProcessing_mapsEntitiesToDomain() {
        UUID id = UUID.randomUUID();
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        Upload domain = new Upload(id, UUID.randomUUID(), "v.mp4", 1L, "video/mp4",
                "uploads/" + id, "mp", UploadStatus.COMPLETED, null,
                LocalDateTime.now(), LocalDateTime.now());
        when(uploadJpa.findStaleForReconciliation(eq(UploadStatus.COMPLETED), eq(cutoff), eq(5)))
                .thenReturn(List.of(UploadEntity.fromDomain(domain)));

        List<Upload> result = adapter.findStaleCompletedWithoutProcessing(cutoff, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(id);
    }
}
