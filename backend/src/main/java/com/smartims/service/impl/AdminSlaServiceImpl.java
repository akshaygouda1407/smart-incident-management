package com.smartims.service.impl;

import com.smartims.dto.UpdateSlaPolicyRequest;
import com.smartims.entity.SlaPolicy;
import com.smartims.repository.SlaPolicyRepository;
import com.smartims.service.AdminSlaService;
import com.smartims.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSlaServiceImpl extends AdminSlaService {

    private final SlaPolicyRepository slaPolicyRepository;
    private final AuditLogService auditLogService;

    @Override
    public SlaPolicy updatePolicy(
            Long projectId,
            String priorityLevel,
            UpdateSlaPolicyRequest request) {

        SlaPolicy policy = slaPolicyRepository
                .findByProjectIdAndPriorityLevel(projectId, priorityLevel)
                .orElseThrow(() ->
                        new RuntimeException(
                                "SLA policy not found for priority " + priorityLevel)
                );

        Integer oldResolutionTime = policy.getResolutionTimeMinutes();

        policy.setResolutionTimeMinutes(
                toMinutes(request.getResolutionTimeMinutes())
        );

        SlaPolicy updatedPolicy = slaPolicyRepository.save(policy);

        auditLogService.log(
                "SLA_POLICY_UPDATED",
                "SLA_POLICY",
                updatedPolicy.getId(),
                "SLA resolution time updated from "
                        + oldResolutionTime
                        + " to "
                        + updatedPolicy.getResolutionTimeMinutes()
                        + " minutes for priority "
                        + priorityLevel
        );

        return updatedPolicy;
    }

    private Integer toMinutes(long minutes) {
        if (minutes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Resolution time is too large"
            );
        }
        return (int) minutes;
    }
}
