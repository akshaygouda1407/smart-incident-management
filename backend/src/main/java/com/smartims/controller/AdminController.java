package com.smartims.controller;

import com.smartims.dto.AdminOverviewResponse;
import com.smartims.dto.AssignIssueRequest;
import com.smartims.dto.UpdatePriorityRequest;
import com.smartims.dto.ApiResponse;
import com.smartims.entity.Issue;
import com.smartims.service.AdminService;
import com.smartims.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> adminDashboard() {

        return ResponseUtil.success(
                HttpStatus.OK,
                "Admin dashboard access granted",
                "Admin dashboard access granted"
        );
    }


    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getOverview() {

        AdminOverviewResponse response = adminService.getOverview();

        return ResponseUtil.success(
                HttpStatus.OK,
                "Admin overview fetched successfully",
                response
        );
    }


    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<List<Issue>>> getAllIssues() {

        List<Issue> issues = adminService.getAllIssues();

        return ResponseUtil.success(
                HttpStatus.OK,
                "Issues fetched successfully",
                issues
        );
    }


    @PutMapping("/issues/{id}/priority")
    public ResponseEntity<ApiResponse<Void>> updatePriority(
            @PathVariable Long id,
            @RequestBody UpdatePriorityRequest request) {

        adminService.updatePriority(id, request.getPriority());

        return ResponseUtil.success(
                HttpStatus.OK,
                "Priority updated successfully",
                null
        );
    }


    @PutMapping("/issues/{id}/assign")
    public ResponseEntity<ApiResponse<Void>> assignEngineer(
            @PathVariable Long id,
            @RequestBody AssignIssueRequest request) {

        adminService.assignEngineer(id, request.getEngineerId());

        return ResponseUtil.success(
                HttpStatus.OK,
                "Engineer assigned successfully",
                null
        );
    }
}
