package br.com.fiapx.upload.application.service;

import br.com.fiapx.upload.application.port.input.UploadUseCase;
import br.com.fiapx.upload.application.port.output.EventPublisherPort;
import br.com.fiapx.upload.application.port.output.StoragePort;
import br.com.fiapx.upload.application.port.output.UploadRepositoryPort;
import br.com.fiapx.upload.domain.exception.FileSizeExceededException;
import br.com.fiapx.upload.domain.exception.UploadNotFoundException;
import br.com.fiapx.upload.domain.exception.UploadNotOwnedByUserException;
import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService implements UploadUseCase {

    private final UploadRepositoryPort uploadRepository;
    private final StoragePort storage;
    private final EventPublisherPort eventPublisher;
    private final long maxFileSizeBytes;
    private final long maxChunkSizeBytes;

    public UploadService(
            UploadRepositoryPort uploadRepository,
            StoragePort storage,
            EventPublisherPort eventPublisher,
            @Value("${upload.max-file-size-bytes}") long maxFileSizeBytes,
            @Value("${upload.max-chunk-size-bytes}") long maxChunkSizeBytes) {
        this.uploadRepository = uploadRepository;
        this.storage = storage;
        this.eventPublisher = eventPublisher;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.maxChunkSizeBytes = maxChunkSizeBytes;
    }

    @Override
    @Transactional
    public Upload initiateUpload(UUID userId, String filename, Long fileSize, String mimeType) {
        if (fileSize != null && fileSize > maxFileSizeBytes) {
            throw new FileSizeExceededException(maxFileSizeBytes);
        }
        Upload upload = Upload.initiate(userId, filename, fileSize, mimeType);
        upload = uploadRepository.save(upload);

        String multipartUploadId = storage.initiateMultipartUpload(upload.s3Key(), mimeType != null ? mimeType : "application/octet-stream");
        upload = upload.withUploadId(multipartUploadId);
        return uploadRepository.save(upload);
    }

    @Override
    @Transactional
    public UploadChunk uploadChunk(UUID uploadId, UUID userId, int chunkNumber, InputStream data, long dataSize) {
        if (dataSize > maxChunkSizeBytes) {
            throw new FileSizeExceededException(maxChunkSizeBytes);
        }
        Upload upload = findAndValidateOwnership(uploadId, userId);
        String etag = storage.uploadPart(upload.s3Key(), upload.uploadId(), chunkNumber, data, dataSize);
        UploadChunk chunk = UploadChunk.create(uploadId, chunkNumber, etag);
        return uploadRepository.saveChunk(chunk);
    }

    @Override
    @Transactional
    public Upload completeUpload(UUID uploadId, UUID userId) {
        Upload upload = findAndValidateOwnership(uploadId, userId);
        List<UploadChunk> chunks = uploadRepository.findChunksByUploadId(uploadId);

        List<StoragePort.PartInfo> parts = chunks.stream()
                .sorted((a, b) -> Integer.compare(a.chunkNumber(), b.chunkNumber()))
                .map(c -> new StoragePort.PartInfo(c.chunkNumber(), c.etag()))
                .toList();

        storage.completeMultipartUpload(upload.s3Key(), upload.uploadId(), parts);
        Upload completed = upload.completed();
        completed = uploadRepository.save(completed);
        eventPublisher.publishUploadCompleted(completed);
        return completed;
    }

    @Override
    public Upload getUpload(UUID uploadId, UUID userId) {
        return findAndValidateOwnership(uploadId, userId);
    }

    @Override
    public List<Upload> getUserUploads(UUID userId) {
        return uploadRepository.findByUserId(userId);
    }

    private Upload findAndValidateOwnership(UUID uploadId, UUID userId) {
        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new UploadNotFoundException(uploadId));
        if (!upload.userId().equals(userId)) {
            throw new UploadNotOwnedByUserException();
        }
        return upload;
    }
}
