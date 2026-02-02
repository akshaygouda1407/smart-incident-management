package com.smartims.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private String managerName;
    private List<String> memberNames;

    private LocalDateTime createdAt;
}
