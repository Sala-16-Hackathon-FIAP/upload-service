package br.com.fiapx.upload.infrastructure.messaging;

import br.com.fiapx.upload.application.port.input.UploadReconciliationUseCase;
import com.autoflow.rabbit_topic_lib.core.TopicConsumer;
import com.autoflow.rabbit_topic_lib.model.TopicBinding;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Observes the processor's lifecycle events so the upload-service knows an
 * upload was actually picked up for processing. Any of started/completed/failed
 * marks the upload as acknowledged, which stops the reconciler from
 * re-publishing it. See {@link UploadReconciliationUseCase}.
 */
@Component
public class ProcessingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProcessingEventConsumer.class);

    static final String EXCHANGE = "fiapx.events";
    static final String QUEUE_STARTED = "upload.video.processing.started";
    static final String QUEUE_COMPLETED = "upload.video.processing.completed";
    static final String QUEUE_FAILED = "upload.video.processing.failed";

    private final TopicConsumer topicConsumer;
    private final UploadReconciliationUseCase reconciliationUseCase;

    public ProcessingEventConsumer(TopicConsumer topicConsumer,
                                   UploadReconciliationUseCase reconciliationUseCase) {
        this.topicConsumer = topicConsumer;
        this.reconciliationUseCase = reconciliationUseCase;
    }

    @PostConstruct
    public void registerConsumers() {
        topicConsumer.consume(new TopicBinding(EXCHANGE, "video.processing.started", QUEUE_STARTED),
                ProcessingJobEvent.class, this::handle);
        topicConsumer.consume(new TopicBinding(EXCHANGE, "video.processing.completed", QUEUE_COMPLETED),
                ProcessingJobEvent.class, this::handle);
        topicConsumer.consume(new TopicBinding(EXCHANGE, "video.processing.failed", QUEUE_FAILED),
                ProcessingJobEvent.class, this::handle);
    }

    public void handle(ProcessingJobEvent event) {
        log.debug("Processing acknowledged for uploadId={} (status={})", event.uploadId(), event.status());
        reconciliationUseCase.acknowledgeProcessingStarted(event.uploadId());
    }

    public record ProcessingJobEvent(
            UUID jobId,
            UUID uploadId,
            UUID userId,
            String filename,
            String resultS3Key,
            String status,
            String errorMessage,
            LocalDateTime timestamp
    ) {}
}
