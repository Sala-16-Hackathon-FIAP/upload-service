package br.com.fiapx.upload.infrastructure.scheduling;

import br.com.fiapx.upload.application.port.input.UploadReconciliationUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically triggers reconciliation of uploads whose processing was never
 * acknowledged. Disabled by setting {@code upload.reconciliation.enabled=false}.
 */
@Component
@ConditionalOnProperty(value = "upload.reconciliation.enabled", havingValue = "true", matchIfMissing = true)
public class UploadReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(UploadReconciliationScheduler.class);

    private final UploadReconciliationUseCase reconciliationUseCase;

    public UploadReconciliationScheduler(UploadReconciliationUseCase reconciliationUseCase) {
        this.reconciliationUseCase = reconciliationUseCase;
    }

    @Scheduled(fixedDelayString = "${upload.reconciliation.interval-ms:120000}")
    public void run() {
        try {
            int republished = reconciliationUseCase.reconcileStuckUploads();
            if (republished > 0) {
                log.info("Reconciliation cycle re-published {} upload(s)", republished);
            }
        } catch (Exception e) {
            log.error("Reconciliation cycle failed", e);
        }
    }
}
