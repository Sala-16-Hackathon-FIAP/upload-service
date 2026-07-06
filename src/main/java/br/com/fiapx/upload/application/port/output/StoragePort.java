package br.com.fiapx.upload.application.port.output;

import java.io.InputStream;
import java.util.List;

public interface StoragePort {
    String initiateMultipartUpload(String key, String contentType);
    String uploadPart(String key, String uploadId, int partNumber, InputStream data, long contentLength);
    void completeMultipartUpload(String key, String uploadId, List<PartInfo> parts);
    void abortMultipartUpload(String key, String uploadId);

    record PartInfo(int partNumber, String etag) {}
}
