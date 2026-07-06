package br.com.fiapx.upload.infrastructure.s3;

import br.com.fiapx.upload.application.port.output.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.List;

@Component
public class S3Adapter implements StoragePort {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Adapter(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String initiateMultipartUpload(String key, String contentType) {
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(r -> r
                .bucket(bucketName)
                .key(key)
                .contentType(contentType));
        return response.uploadId();
    }

    @Override
    public String uploadPart(String key, String uploadId, int partNumber, InputStream data, long contentLength) {
        UploadPartResponse response = s3Client.uploadPart(
                r -> r.bucket(bucketName).key(key).uploadId(uploadId).partNumber(partNumber)
                        .checksumAlgorithm((String) null),
                RequestBody.fromInputStream(data, contentLength));
        return response.eTag();
    }

    @Override
    public void completeMultipartUpload(String key, String uploadId, List<PartInfo> parts) {
        List<CompletedPart> completedParts = parts.stream()
                .map(p -> CompletedPart.builder().partNumber(p.partNumber()).eTag(p.etag()).build())
                .toList();
        s3Client.completeMultipartUpload(r -> r
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(m -> m.parts(completedParts)));
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {
        s3Client.abortMultipartUpload(r -> r.bucket(bucketName).key(key).uploadId(uploadId));
    }
}
