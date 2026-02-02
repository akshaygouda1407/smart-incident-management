package com.smartims.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IssueActivityResponse {

    private String action;
    private String description;
    private String performedBy;
    private LocalDateTime createdAt;
}
