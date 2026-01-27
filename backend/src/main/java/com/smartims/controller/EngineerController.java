package com.smartims.controller;

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
    public Object getDashboard(Authentication authentication) {
        return engineerService.getDashboard(authentication.getName());
    }

    @GetMapping("/issues")
    public List<Issue> getMyIssues(Authentication authentication) {
        return engineerService.getMyIssues(authentication.getName());
    }

    @PutMapping("/issues/{id}/status")
    public String updateIssueStatus(
            @PathVariable Long id,
            Authentication authentication) {

        engineerService.updateIssueStatus(id, authentication.getName());
        return "Issue status updated successfully";
    }
}
