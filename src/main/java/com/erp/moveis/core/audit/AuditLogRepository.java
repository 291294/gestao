package com.erp.moveis.core.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);

    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
