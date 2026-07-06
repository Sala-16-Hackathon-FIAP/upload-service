package br.com.fiapx.upload.application.service;

import br.com.fiapx.upload.application.port.output.EventPublisherPort;
import br.com.fiapx.upload.application.port.output.StoragePort;
import br.com.fiapx.upload.application.port.output.UploadRepositoryPort;
import br.com.fiapx.upload.domain.exception.FileSizeExceededException;
import br.com.fiapx.upload.domain.exception.UploadNotFoundException;
import br.com.fiapx.upload.domain.exception.UploadNotOwnedByUserException;
import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadChunk;
import br.com.fiapx.upload.domain.model.UploadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock private UploadRepositoryPort uploadRepository;
    @Mock private StoragePort storage;
    @Mock private EventPublisherPort eventPublisher;

    private UploadService uploadService;
    private UUID userId;
    private Upload sampleUpload;

    @BeforeEach
    void setUp() {
        uploadService = new UploadService(uploadRepository, storage, eventPublisher,
                2147483648L, 10485760L);
        userId = UUID.randomUUID();
        sampleUpload = new Upload(UUID.randomUUID(), userId, "video.mp4", 1024L,
                "video/mp4", "uploads/" + userId + "/test/video.mp4", "s3-multipart-id",
                UploadStatus.UPLOADING, null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void initiateUpload_shouldCreateUpload_whenFileSizeIsWithinLimit() {
        when(storage.initiateMultipartUpload(any(), any())).thenReturn("multipart-id");
        when(uploadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Upload result = uploadService.initiateUpload(userId, "video.mp4", 1024L, "video/mp4");

        assertThat(result.status()).isEqualTo(UploadStatus.UPLOADING);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.uploadId()).isEqualTo("multipart-id");
        verify(storage).initiateMultipartUpload(any(), eq("video/mp4"));
    }

    @Test
    void initiateUpload_shouldThrow_whenFileSizeExceedsLimit() {
        assertThatThrownBy(() -> uploadService.initiateUpload(userId, "big.mp4", 3_000_000_000L, "video/mp4"))
                .isInstanceOf(FileSizeExceededException.class);
        verifyNoInteractions(storage);
    }

    @Test
    void initiateUpload_shouldAcceptNullMimeType() {
        when(storage.initiateMultipartUpload(any(), eq("application/octet-stream"))).thenReturn("id");
        when(uploadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Upload result = uploadService.initiateUpload(userId, "file.bin", 100L, null);
        assertThat(result).isNotNull();
    }

    @Test
    void uploadChunk_shouldSaveChunk_whenDataSizeIsWithinLimit() {
        when(uploadRepository.findById(sampleUpload.id())).thenReturn(Optional.of(sampleUpload));
        when(storage.uploadPart(any(), any(), eq(1), any(), eq(1024L))).thenReturn("etag-1");
        when(uploadRepository.saveChunk(any())).thenAnswer(inv -> inv.getArgument(0));

        UploadChunk chunk = uploadService.uploadChunk(sampleUpload.id(), userId, 1,
                new ByteArrayInputStream(new byte[1024]), 1024L);

        assertThat(chunk.chunkNumber()).isEqualTo(1);
        assertThat(chunk.etag()).isEqualTo("etag-1");
    }

    @Test
    void uploadChunk_shouldThrow_whenChunkTooLarge() {
        assertThatThrownBy(() -> uploadService.uploadChunk(sampleUpload.id(), userId, 1,
                new ByteArrayInputStream(new byte[0]), 11_000_000L))
                .isInstanceOf(FileSizeExceededException.class);
    }

    @Test
    void uploadChunk_shouldThrow_whenUploadNotFound() {
        when(uploadRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadService.uploadChunk(UUID.randomUUID(), userId, 1,
                new ByteArrayInputStream(new byte[0]), 100L))
                .isInstanceOf(UploadNotFoundException.class);
    }

    @Test
    void uploadChunk_shouldThrow_whenUploadNotOwnedByUser() {
        Upload otherUser = new Upload(sampleUpload.id(), UUID.randomUUID(), "v.mp4", 100L,
                "video/mp4", "key", "mid", UploadStatus.UPLOADING, null,
                LocalDateTime.now(), LocalDateTime.now());
        when(uploadRepository.findById(sampleUpload.id())).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> uploadService.uploadChunk(sampleUpload.id(), userId, 1,
                new ByteArrayInputStream(new byte[0]), 100L))
                .isInstanceOf(UploadNotOwnedByUserException.class);
    }

    @Test
    void completeUpload_shouldCompleteAndPublishEvent() {
        when(uploadRepository.findById(sampleUpload.id())).thenReturn(Optional.of(sampleUpload));
        UploadChunk chunk1 = new UploadChunk(UUID.randomUUID(), sampleUpload.id(), 1, "etag1", LocalDateTime.now());
        UploadChunk chunk2 = new UploadChunk(UUID.randomUUID(), sampleUpload.id(), 2, "etag2", LocalDateTime.now());
        when(uploadRepository.findChunksByUploadId(sampleUpload.id())).thenReturn(List.of(chunk1, chunk2));
        when(uploadRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Upload result = uploadService.completeUpload(sampleUpload.id(), userId);

        assertThat(result.status()).isEqualTo(UploadStatus.COMPLETED);
        verify(storage).completeMultipartUpload(any(), any(), argThat(parts -> parts.size() == 2));
        verify(eventPublisher).publishUploadCompleted(any());
    }

    @Test
    void getUserUploads_shouldReturnUploadsForUser() {
        when(uploadRepository.findByUserId(userId)).thenReturn(List.of(sampleUpload));
        List<Upload> uploads = uploadService.getUserUploads(userId);
        assertThat(uploads).hasSize(1);
    }
}
