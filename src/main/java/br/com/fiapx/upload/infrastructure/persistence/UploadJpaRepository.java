package br.com.fiapx.upload.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadJpaRepository extends JpaRepository<UploadEntity, UUID> {
    List<UploadEntity> findByUserId(UUID userId);
}
