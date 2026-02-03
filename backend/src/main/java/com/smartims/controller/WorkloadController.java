package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import com.smartims.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadService workloadService;

    @GetMapping("/engineer/{engineerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public ApiResponse<Long> getEngineerWorkload(
            @PathVariable Long engineerId) {

        return ApiResponse.success(
                "Engineer workload fetched successfully",
                workloadService.getEngineerWorkload(engineerId)
        );
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Long> getManagerWorkload(
            @PathVariable Long managerId) {

        return ApiResponse.success(
                "Manager workload fetched successfully",
                workloadService.getManagerWorkload(managerId)
        );
    }
}
