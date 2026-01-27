package com.smartims.controller;

import com.smartims.dto.AdminOverviewResponse;
import com.smartims.dto.AssignIssueRequest;
import com.smartims.dto.UpdatePriorityRequest;
import com.smartims.entity.Issue;
import com.smartims.service.AdminService;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "Admin dashboard access granted 👑";
    }

    @GetMapping("/overview")
    public AdminOverviewResponse getOverview() {
        return adminService.getOverview();
    }

    @GetMapping("/issues")
    public List<Issue> getAllIssues() {
        return adminService.getAllIssues();
    }

    @PutMapping("/issues/{id}/priority")
    public ResponseEntity<?> updatePriority(
            @PathVariable Long id,
            @RequestBody UpdatePriorityRequest request) {

        adminService.updatePriority(id, request.getPriority());
        return ResponseEntity.ok("Priority updated successfully");
    }

    @PutMapping("/issues/{id}/assign")
    public ResponseEntity<?> assignEngineer(
            @PathVariable Long id,
            @RequestBody AssignIssueRequest request) {

        adminService.assignEngineer(id, request.getEngineerId());
        return ResponseEntity.ok("Engineer assigned successfully");
    }
}
