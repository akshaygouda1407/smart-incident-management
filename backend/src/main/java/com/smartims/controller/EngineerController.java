package com.smartims.controller;

import com.smartims.dto.ApiResponse;
import com.smartims.dto.EngineerDashboardResponse;
import com.smartims.entity.Issue;
import com.smartims.service.EngineerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engineer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENGINEER')")
public class EngineerController {

    private final EngineerService engineerService;

    @GetMapping("/dashboard")
    public ApiResponse<EngineerDashboardResponse> getDashboard(Authentication authentication) {
        return ApiResponse.success(
                "Engineer dashboard fetched successfully",
                engineerService.getDashboard(authentication.getName())
        );
    }

    @GetMapping("/issues")
    public ApiResponse<List<Issue>> getMyIssues(Authentication authentication) {
        return ApiResponse.success(
                "Engineer issues fetched successfully",
                engineerService.getMyIssues(authentication.getName())
        );
    }

    @PutMapping("/issues/{id}/status")
    public ApiResponse<Object> updateIssueStatus(
            @PathVariable Long id,
            Authentication authentication) {

        engineerService.updateIssueStatus(id, authentication.getName());

        return ApiResponse.success(
                "Issue status updated successfully",
                null
        );
    }
}
