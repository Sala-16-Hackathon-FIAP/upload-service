package br.com.fiapx.upload.application.service;

import br.com.fiapx.upload.application.port.output.EventPublisherPort;
import br.com.fiapx.upload.application.port.output.UploadRepositoryPort;
import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadReconciliationServiceTest {

    @Mock private UploadRepositoryPort uploadRepository;
    @Mock private EventPublisherPort eventPublisher;

    private UploadReconciliationService service;

    @BeforeEach
    void setUp() {
        service = new UploadReconciliationService(uploadRepository, eventPublisher, 10L, 5);
    }

    private Upload upload() {
        UUID id = UUID.randomUUID();
        return new Upload(id, UUID.randomUUID(), "v.mp4", 100L, "video/mp4",
                "uploads/" + id, "mp-id", UploadStatus.COMPLETED, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void acknowledgeProcessingStarted_marksUpload() {
        UUID uploadId = UUID.randomUUID();
        service.acknowledgeProcessingStarted(uploadId);
        verify(uploadRepository).markProcessingStarted(eq(uploadId), any(LocalDateTime.class));
    }

    @Test
    void reconcile_republishesAndIncrementsEachStuckUpload() {
        Upload a = upload();
        Upload b = upload();
        when(uploadRepository.findStaleCompletedWithoutProcessing(any(), eq(5)))
                .thenReturn(List.of(a, b));

        int count = service.reconcileStuckUploads();

        assertThat(count).isEqualTo(2);
        verify(eventPublisher).publishUploadCompleted(a);
        verify(eventPublisher).publishUploadCompleted(b);
        verify(uploadRepository).incrementReconciliationAttempts(a.id());
        verify(uploadRepository).incrementReconciliationAttempts(b.id());
    }

    @Test
    void reconcile_doesNothing_whenNoStuckUploads() {
        when(uploadRepository.findStaleCompletedWithoutProcessing(any(), eq(5)))
                .thenReturn(List.of());

        int count = service.reconcileStuckUploads();

        assertThat(count).isZero();
        verify(eventPublisher, never()).publishUploadCompleted(any());
        verify(uploadRepository, never()).incrementReconciliationAttempts(any());
    }
}
