package br.com.fiapx.upload.application.port.input;

import java.util.UUID;

/**
 * Safety net for the choreographed saga. Because the RabbitMQ broker is not
 * durable in this environment, a video.upload.completed event can be lost. The
 * upload itself is always persisted (S3 + database) before the event is
 * published, so no work is truly lost: uploads that completed but were never
 * picked up by the processor are detected and their event re-published.
 */
public interface UploadReconciliationUseCase {

    /** Records that the processor started working on an upload (from a processing.started event). */
    void acknowledgeProcessingStarted(UUID uploadId);

    /**
     * Re-publishes video.upload.completed for COMPLETED uploads that have had no
     * processing acknowledgement past the stale threshold.
     *
     * @return the number of uploads re-published
     */
    int reconcileStuckUploads();
}
