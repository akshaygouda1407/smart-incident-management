package com.smartims.controller;

import com.smartims.dto.CreateIssueRequest;
import com.smartims.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @PostMapping
    public String createIssue(
            @Valid @RequestBody CreateIssueRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName(); // from JWT

        issueService.createIssue(request, email);

        return "Issue created successfully";
    }

    @PostMapping("/debug")
    public Object debug(Authentication authentication) {
        return authentication;
    }

}


