package com.smartims.mapper;

import com.smartims.dto.SlaResponse;
import com.smartims.entity.SlaPolicy;

public class SlaMapper {

    public static SlaResponse toResponse(SlaPolicy sla) {
        Long projectId = sla.getProject() != null ? sla.getProject().getId() : null;
        String projectName = sla.getProject() != null ? sla.getProject().getName() : null;

        return SlaResponse.builder()
                .id(sla.getId())
                .priorityLevel(sla.getPriorityLevel())
                .resolutionTimeMinutes(sla.getResolutionTimeMinutes())
                .description(sla.getDescription())
                .projectId(projectId)
                .projectName(projectName)
                .build();
    }
}
