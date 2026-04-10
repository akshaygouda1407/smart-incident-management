package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import com.smartims.dto.DashboardSummaryResponse;
import com.smartims.dto.KeyValueCountResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.Project;
import com.smartims.entity.User;
import com.smartims.enums.Role;
import com.smartims.exception.UnauthorizedException;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.ProjectRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

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
    public ApiResponse<List<Issue>> getSlaBreaches(Authentication auth) {
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<Issue> issues;

        if (currentUser.getRole() == Role.ADMIN) {
            String company = requireCompany(currentUser);
            issues = issueRepository.findBySlaBreachedTrueAndProject_Company(company);
        } else if (currentUser.getRole() == Role.MANAGER) {
            List<Project> projects = projectRepository.findByManager(currentUser);
            issues = projects.stream()
                    .flatMap(p -> issueRepository.findByProject(p).stream())
                    .filter(Issue::isSlaBreached)
                    .toList();
        } else {
            throw new UnauthorizedException("Access denied");
        }

        return ApiResponse.success("SLA breached issues fetched successfully", issues);
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

    private String requireCompany(User user) {
        String company = user.getCompany();
        if (company == null || company.isBlank()) {
            throw new UnauthorizedException("Company not set for user");
        }
        return company.trim();
    }
}
