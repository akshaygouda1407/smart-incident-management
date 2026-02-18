package com.smartims.repository;

import com.smartims.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActorEmail(String actorEmail);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    @Query("""
            select a from AuditLog a
            join User u on a.actorEmail = u.email
            where u.company = :company
            """)
    List<AuditLog> findByActorCompany(@Param("company") String company);
}
