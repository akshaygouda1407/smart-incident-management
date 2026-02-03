package com.smartims.controller;

import com.smartims.dto.ApiResponse;
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
    public ApiResponse<List<AuditLog>> getAllLogs() {
        return ApiResponse.success(
                "Audit logs fetched successfully",
                auditLogRepository.findAll()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER','USER')")
    @GetMapping("/me")
    public ApiResponse<List<AuditLog>> getMyLogs(Authentication auth) {
        return ApiResponse.success(
                "My audit logs fetched successfully",
                auditLogRepository.findByActorEmail(auth.getName())
        );
    }
}
