package com.smartims.controller;

import com.smartims.dto.IssueAttachmentResponse;
import com.smartims.service.IssueAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class IssueAttachmentController {

    private final IssueAttachmentService attachmentService;

    // Upload
    @PostMapping("/api/issues/{issueId}/attachments")
    public ResponseEntity<IssueAttachmentResponse> upload(
            @PathVariable Long issueId,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(
                attachmentService.upload(issueId, file)
        );
    }

    // List
    @GetMapping("/api/issues/{issueId}/attachments")
    public ResponseEntity<List<IssueAttachmentResponse>> list(
            @PathVariable Long issueId) {

        return ResponseEntity.ok(
                attachmentService.getAttachments(issueId)
        );
    }

    // Download
    @GetMapping("/api/issues/attachments/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {

        Resource file = attachmentService.download(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
