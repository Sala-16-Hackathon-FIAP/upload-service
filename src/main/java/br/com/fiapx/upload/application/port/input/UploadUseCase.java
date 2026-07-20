package br.com.fiapx.upload.application.port.input;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadChunk;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface UploadUseCase {
    Upload initiateUpload(UUID userId, String filename, Long fileSize, String mimeType);
    UploadChunk uploadChunk(UUID uploadId, UUID userId, int chunkNumber, InputStream data, long dataSize);
    Upload completeUpload(UUID uploadId, UUID userId);
    Upload getUpload(UUID uploadId, UUID userId);
    List<Upload> getUserUploads(UUID userId);
}
