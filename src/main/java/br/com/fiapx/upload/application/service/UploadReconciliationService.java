package br.com.fiapx.upload.application.service;

import br.com.fiapx.upload.application.port.input.UploadReconciliationUseCase;
import br.com.fiapx.upload.application.port.output.EventPublisherPort;
import br.com.fiapx.upload.application.port.output.UploadRepositoryPort;
import br.com.fiapx.upload.domain.model.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UploadReconciliationService implements UploadReconciliationUseCase {

    private static final Logger log = LoggerFactory.getLogger(UploadReconciliationService.class);

    private final UploadRepositoryPort uploadRepository;
    private final EventPublisherPort eventPublisher;
    private final long staleAfterMinutes;
    private final int maxAttempts;

    public UploadReconciliationService(
            UploadRepositoryPort uploadRepository,
            EventPublisherPort eventPublisher,
            @Value("${upload.reconciliation.stale-after-minutes:10}") long staleAfterMinutes,
            @Value("${upload.reconciliation.max-attempts:5}") int maxAttempts) {
        this.uploadRepository = uploadRepository;
        this.eventPublisher = eventPublisher;
        this.staleAfterMinutes = staleAfterMinutes;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void acknowledgeProcessingStarted(UUID uploadId) {
        uploadRepository.markProcessingStarted(uploadId, LocalDateTime.now());
    }

    @Override
    public int reconcileStuckUploads() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(staleAfterMinutes);
        List<Upload> stuck = uploadRepository.findStaleCompletedWithoutProcessing(cutoff, maxAttempts);
        if (stuck.isEmpty()) {
            return 0;
        }
        log.warn("Reconciliation: re-publishing {} stuck upload(s) with no processing after {} min",
                stuck.size(), staleAfterMinutes);
        for (Upload upload : stuck) {
            log.info("Reconciliation: re-publishing upload.completed for uploadId={}", upload.id());
            eventPublisher.publishUploadCompleted(upload);
            uploadRepository.incrementReconciliationAttempts(upload.id());
        }
        return stuck.size();
    }
}
