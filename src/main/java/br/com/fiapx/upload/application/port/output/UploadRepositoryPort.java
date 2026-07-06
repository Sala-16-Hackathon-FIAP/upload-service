package br.com.fiapx.upload.application.port.output;

import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadChunk;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadRepositoryPort {
    Upload save(Upload upload);
    Optional<Upload> findById(UUID id);
    List<Upload> findByUserId(UUID userId);
    UploadChunk saveChunk(UploadChunk chunk);
    List<UploadChunk> findChunksByUploadId(UUID uploadId);
}
