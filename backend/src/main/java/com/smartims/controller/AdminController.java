package com.smartims.controller;

import com.smartims.dto.AdminOverviewResponse;
import com.smartims.dto.AssignIssueRequest;
import com.smartims.dto.UpdatePriorityRequest;
import com.smartims.dto.ApiResponse;
import com.smartims.entity.Issue;
import com.smartims.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ApiResponse<Object> adminDashboard() {

        return ApiResponse.success("Admin dashboard access granted");
    }


    @GetMapping("/overview")
    public ApiResponse<AdminOverviewResponse> getOverview() {

        AdminOverviewResponse response = adminService.getOverview();

        return ApiResponse.success(
                "Admin overview fetched successfully",
                response
        );
    }


    @GetMapping("/issues")
    public ApiResponse<List<Issue>> getAllIssues() {

        List<Issue> issues = adminService.getAllIssues();

        return ApiResponse.success(
                "Issues fetched successfully",
                issues
        );
    }


    @PutMapping("/issues/{id}/priority")
    public ApiResponse<Object> updatePriority(
            @PathVariable Long id,
            @RequestBody UpdatePriorityRequest request) {

        adminService.updatePriority(id, request.getPriority());

        return ApiResponse.success(
                "Priority updated successfully",
                null
        );
    }


    @PutMapping("/issues/{id}/assign")
    public ApiResponse<Object> assignEngineer(
            @PathVariable Long id,
            @RequestBody AssignIssueRequest request) {

        adminService.assignEngineer(id, request.getEngineerId());

        return ApiResponse.success(
                "Engineer assigned successfully",
                null
        );
    }
}
