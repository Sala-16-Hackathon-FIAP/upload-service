package br.com.fiapx.upload.infrastructure.messaging;

import br.com.fiapx.upload.application.port.output.EventPublisherPort;
import br.com.fiapx.upload.domain.model.Upload;
import com.autoflow.rabbit_topic_lib.core.TopicPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class UploadEventPublisher implements EventPublisherPort {

    static final String EXCHANGE = "fiapx.events";
    static final String ROUTING_KEY_UPLOAD_COMPLETED = "video.upload.completed";

    private final TopicPublisher topicPublisher;

    public UploadEventPublisher(TopicPublisher topicPublisher) {
        this.topicPublisher = topicPublisher;
    }

    @Override
    public void publishUploadCompleted(Upload upload) {
        VideoUploadCompletedEvent event = new VideoUploadCompletedEvent(
                upload.id(), upload.userId(), upload.originalFilename(),
                upload.s3Key(), upload.mimeType(), LocalDateTime.now());
        topicPublisher.publish(EXCHANGE, ROUTING_KEY_UPLOAD_COMPLETED, event);
    }

    public record VideoUploadCompletedEvent(
        UUID uploadId,
        UUID userId,
        String filename,
        String s3Key,
        String mimeType,
        LocalDateTime uploadedAt
    ) {}
}
