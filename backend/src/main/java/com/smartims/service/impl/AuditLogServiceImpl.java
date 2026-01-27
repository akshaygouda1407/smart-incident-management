package com.smartims.service.impl;

import com.smartims.entity.AuditLog;
import com.smartims.entity.User;
import com.smartims.repository.AuditLogRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public void log(String action, String entityType, Long entityId, String description) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuditLog log = AuditLog.builder()
                .actorEmail(user.getEmail())
                .actorRole(user.getRole().name())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}
