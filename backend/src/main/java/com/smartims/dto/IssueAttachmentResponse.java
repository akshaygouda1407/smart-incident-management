package com.smartims.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IssueAttachmentResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
