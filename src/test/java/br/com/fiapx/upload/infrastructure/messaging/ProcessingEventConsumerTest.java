package br.com.fiapx.upload.infrastructure.messaging;

import br.com.fiapx.upload.application.port.input.UploadReconciliationUseCase;
import com.autoflow.rabbit_topic_lib.core.TopicConsumer;
import com.autoflow.rabbit_topic_lib.model.TopicBinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessingEventConsumerTest {

    @Mock private TopicConsumer topicConsumer;
    @Mock private UploadReconciliationUseCase reconciliationUseCase;

    @InjectMocks
    private ProcessingEventConsumer consumer;

    @Test
    void registerConsumers_bindsStartedCompletedAndFailed() {
        consumer.registerConsumers();
        verify(topicConsumer, times(3)).consume(any(TopicBinding.class),
                eq(ProcessingEventConsumer.ProcessingJobEvent.class), any());
    }

    @Test
    void handle_acknowledgesProcessingForUpload() {
        UUID uploadId = UUID.randomUUID();
        ProcessingEventConsumer.ProcessingJobEvent event = new ProcessingEventConsumer.ProcessingJobEvent(
                UUID.randomUUID(), uploadId, UUID.randomUUID(), "v.mp4", null,
                "PROCESSING", null, LocalDateTime.now());

        consumer.handle(event);

        verify(reconciliationUseCase).acknowledgeProcessingStarted(uploadId);
    }
}
