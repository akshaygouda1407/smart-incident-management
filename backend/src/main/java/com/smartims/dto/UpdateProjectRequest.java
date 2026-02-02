package com.smartims.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateProjectRequest {

    private String name;
    private String description;
    private Long managerId;
    private List<Long> memberIds;
}
