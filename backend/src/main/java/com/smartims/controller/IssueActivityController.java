package com.smartims.controller;

import com.smartims.dto.IssueActivityResponse;
import com.smartims.service.IssueActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/issues/{issueId}/timeline")
@RequiredArgsConstructor
public class IssueActivityController {

    private final IssueActivityService activityService;

    @GetMapping
    public ResponseEntity<List<IssueActivityResponse>> getTimeline(
            @PathVariable Long issueId) {

        return ResponseEntity.ok(
                activityService.getTimeline(issueId)
        );
    }
}
