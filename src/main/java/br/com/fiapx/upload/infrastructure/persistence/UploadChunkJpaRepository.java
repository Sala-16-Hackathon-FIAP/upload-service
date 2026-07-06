package br.com.fiapx.upload.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadChunkJpaRepository extends JpaRepository<UploadChunkEntity, UUID> {
    List<UploadChunkEntity> findByUploadId(UUID uploadId);
}
