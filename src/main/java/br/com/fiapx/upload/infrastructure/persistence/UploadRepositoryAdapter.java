package br.com.fiapx.upload.infrastructure.persistence;

import br.com.fiapx.upload.application.port.output.UploadRepositoryPort;
import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadChunk;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UploadRepositoryAdapter implements UploadRepositoryPort {

    private final UploadJpaRepository uploadJpa;
    private final UploadChunkJpaRepository chunkJpa;

    public UploadRepositoryAdapter(UploadJpaRepository uploadJpa, UploadChunkJpaRepository chunkJpa) {
        this.uploadJpa = uploadJpa;
        this.chunkJpa = chunkJpa;
    }

    @Override
    public Upload save(Upload upload) {
        return uploadJpa.save(UploadEntity.fromDomain(upload)).toDomain();
    }

    @Override
    public Optional<Upload> findById(UUID id) {
        return uploadJpa.findById(id).map(UploadEntity::toDomain);
    }

    @Override
    public List<Upload> findByUserId(UUID userId) {
        return uploadJpa.findByUserId(userId).stream().map(UploadEntity::toDomain).toList();
    }

    @Override
    public UploadChunk saveChunk(UploadChunk chunk) {
        return chunkJpa.save(UploadChunkEntity.fromDomain(chunk)).toDomain();
    }

    @Override
    public List<UploadChunk> findChunksByUploadId(UUID uploadId) {
        return chunkJpa.findByUploadId(uploadId).stream().map(UploadChunkEntity::toDomain).toList();
    }
}
