package br.com.fiapx.upload.application.port.output;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadChunk;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadRepositoryPort {
    Upload save(Upload upload);
    Optional<Upload> findById(UUID id);
    List<Upload> findByUserId(UUID userId);
    UploadChunk saveChunk(UploadChunk chunk);
    List<UploadChunk> findChunksByUploadId(UUID uploadId);

    /** Records that processing has started for an upload (idempotent; only sets it once). */
    void markProcessingStarted(UUID uploadId, LocalDateTime timestamp);

    /** Increments the reconciliation attempt counter for an upload. */
    void incrementReconciliationAttempts(UUID uploadId);

    /**
     * COMPLETED uploads with no processing acknowledgement, idle since before
     * {@code cutoff} and still under the attempt limit.
     */
    List<Upload> findStaleCompletedWithoutProcessing(LocalDateTime cutoff, int maxAttempts);
}
