package com.smartims.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IssueCommentResponse {
    private Long id;
    private String comment;
    private String commentedBy;
    private LocalDateTime createdAt;
}

