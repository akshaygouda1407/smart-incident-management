package com.smartims.controller;

import com.smartims.entity.AuditLog;
import com.smartims.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER','USER')")
    @GetMapping("/me")
    public List<AuditLog> getMyLogs(Authentication auth) {
        return auditLogRepository.findByActorEmail(auth.getName());
    }
}
