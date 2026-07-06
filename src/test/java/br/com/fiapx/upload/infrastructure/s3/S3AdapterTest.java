package br.com.fiapx.upload.infrastructure.s3;

import br.com.fiapx.upload.application.port.output.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3AdapterTest {

    @Mock
    private S3Client s3Client;

    private S3Adapter s3Adapter;

    @BeforeEach
    void setUp() {
        s3Adapter = new S3Adapter(s3Client, "test-bucket");
    }

    @Test
    void initiateMultipartUpload_shouldReturnUploadId() {
        CreateMultipartUploadResponse response = CreateMultipartUploadResponse.builder()
                .uploadId("multipart-upload-id").build();
        when(s3Client.createMultipartUpload(any(Consumer.class))).thenReturn(response);

        String result = s3Adapter.initiateMultipartUpload("key/file.mp4", "video/mp4");

        assertThat(result).isEqualTo("multipart-upload-id");
    }

    @Test
    void uploadPart_shouldReturnETag() {
        UploadPartResponse response = UploadPartResponse.builder().eTag("etag-1").build();
        when(s3Client.uploadPart(any(Consumer.class), any(RequestBody.class))).thenReturn(response);

        String result = s3Adapter.uploadPart("key", "uploadId", 1,
                new ByteArrayInputStream(new byte[10]), 10L);

        assertThat(result).isEqualTo("etag-1");
    }

    @Test
    void completeMultipartUpload_shouldCallS3Client() {
        CompleteMultipartUploadResponse response = CompleteMultipartUploadResponse.builder().build();
        when(s3Client.completeMultipartUpload(any(Consumer.class))).thenReturn(response);

        List<StoragePort.PartInfo> parts = List.of(
                new StoragePort.PartInfo(1, "etag-1"),
                new StoragePort.PartInfo(2, "etag-2"));

        s3Adapter.completeMultipartUpload("key", "uploadId", parts);

        verify(s3Client).completeMultipartUpload(any(Consumer.class));
    }

    @Test
    void abortMultipartUpload_shouldCallS3Client() {
        AbortMultipartUploadResponse response = AbortMultipartUploadResponse.builder().build();
        when(s3Client.abortMultipartUpload(any(Consumer.class))).thenReturn(response);

        s3Adapter.abortMultipartUpload("key", "uploadId");

        verify(s3Client).abortMultipartUpload(any(Consumer.class));
    }
}
