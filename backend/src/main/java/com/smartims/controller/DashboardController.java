package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import com.smartims.dto.DashboardSummaryResponse;
import com.smartims.dto.KeyValueCountResponse;
import com.smartims.entity.Issue;
import com.smartims.repository.IssueRepository;
import com.smartims.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final IssueRepository issueRepository;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {
        return ApiResponse.success(
                "Dashboard summary fetched successfully",
                dashboardService.getSummary()
        );
    }

    @GetMapping("/status")
    public ApiResponse<List<KeyValueCountResponse>> statusWise() {
        return ApiResponse.success(
                "Status-wise distribution fetched successfully",
                dashboardService.getStatusDistribution()
        );
    }

    @GetMapping("/severity")
    public ApiResponse<List<KeyValueCountResponse>> severityWise() {
        return ApiResponse.success(
                "Severity-wise distribution fetched successfully",
                dashboardService.getSeverityDistribution()
        );
    }

    @GetMapping("/priority")
    public ApiResponse<List<KeyValueCountResponse>> priorityWise() {
        return ApiResponse.success(
                "Priority-wise distribution fetched successfully",
                dashboardService.getPriorityDistribution()
        );
    }

    @GetMapping("/sla-breaches")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<List<Issue>> getSlaBreaches() {
        return ApiResponse.success(
                "SLA breached issues fetched successfully",
                issueRepository.findBySlaBreachedTrue()
        );
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER','USER')")
    public ApiResponse<DashboardSummaryResponse> getProjectDashboard(
            @PathVariable Long projectId) {

        return ApiResponse.success(
                "Project dashboard fetched successfully",
                dashboardService.getProjectDashboard(projectId)
        );
    }
}
