package br.com.fiapx.upload.infrastructure.persistence;

import br.com.fiapx.upload.domain.model.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UploadJpaRepository extends JpaRepository<UploadEntity, UUID> {
    List<UploadEntity> findByUserId(UUID userId);

    @Modifying
    @Query("UPDATE UploadEntity u SET u.processingStartedAt = :timestamp " +
           "WHERE u.id = :id AND u.processingStartedAt IS NULL")
    int markProcessingStarted(@Param("id") UUID id, @Param("timestamp") LocalDateTime timestamp);

    @Modifying
    @Query("UPDATE UploadEntity u SET u.reconciliationAttempts = u.reconciliationAttempts + 1 " +
           "WHERE u.id = :id")
    int incrementReconciliationAttempts(@Param("id") UUID id);

    @Query("SELECT u FROM UploadEntity u WHERE u.status = :status " +
           "AND u.processingStartedAt IS NULL AND u.reconciliationAttempts < :maxAttempts " +
           "AND u.updatedAt < :cutoff")
    List<UploadEntity> findStaleForReconciliation(@Param("status") UploadStatus status,
                                                  @Param("cutoff") LocalDateTime cutoff,
                                                  @Param("maxAttempts") int maxAttempts);
}
