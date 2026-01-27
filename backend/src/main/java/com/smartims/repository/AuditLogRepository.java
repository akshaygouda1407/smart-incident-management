package com.smartims.repository;

import com.smartims.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActorEmail(String actorEmail);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
