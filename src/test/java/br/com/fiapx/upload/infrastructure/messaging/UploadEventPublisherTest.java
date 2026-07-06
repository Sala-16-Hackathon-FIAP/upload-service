package br.com.fiapx.upload.infrastructure.messaging;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadStatus;
import com.autoflow.rabbit_topic_lib.core.TopicPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UploadEventPublisherTest {

    @Mock
    private TopicPublisher topicPublisher;

    @InjectMocks
    private UploadEventPublisher publisher;

    @Test
    void publishUploadCompleted_shouldPublishEventWithCorrectRoutingKey() {
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Upload upload = new Upload(uploadId, userId, "video.mp4", 1024L,
                "video/mp4", "uploads/key", "mid", UploadStatus.COMPLETED, null,
                LocalDateTime.now(), LocalDateTime.now());

        publisher.publishUploadCompleted(upload);

        ArgumentCaptor<UploadEventPublisher.VideoUploadCompletedEvent> captor =
                ArgumentCaptor.forClass(UploadEventPublisher.VideoUploadCompletedEvent.class);
        verify(topicPublisher).publish(
                eq("fiapx.events"),
                eq("video.upload.completed"),
                captor.capture());

        UploadEventPublisher.VideoUploadCompletedEvent event = captor.getValue();
        assertThat(event.uploadId()).isEqualTo(uploadId);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.filename()).isEqualTo("video.mp4");
        assertThat(event.s3Key()).isEqualTo("uploads/key");
        assertThat(event.mimeType()).isEqualTo("video/mp4");
        assertThat(event.uploadedAt()).isNotNull();
    }
}
