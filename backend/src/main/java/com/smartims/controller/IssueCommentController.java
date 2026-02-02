package com.smartims.controller;

import com.smartims.dto.IssueCommentRequest;
import com.smartims.dto.IssueCommentResponse;
import com.smartims.service.IssueCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
@RequiredArgsConstructor
public class IssueCommentController {

    private final IssueCommentService commentService;

    // Add comment
    @PostMapping
    public ResponseEntity<IssueCommentResponse> addComment(
            @PathVariable Long issueId,
            @RequestBody IssueCommentRequest request) {

        return ResponseEntity.ok(
                commentService.addComment(issueId, request.getComment())
        );
    }

    // Get comments
    @GetMapping
    public ResponseEntity<List<IssueCommentResponse>> getComments(
            @PathVariable Long issueId) {

        return ResponseEntity.ok(
                commentService.getComments(issueId)
        );
    }
}
