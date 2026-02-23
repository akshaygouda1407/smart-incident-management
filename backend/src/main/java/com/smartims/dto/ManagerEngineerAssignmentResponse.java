package com.smartims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerEngineerAssignmentResponse {
    private Long engineerId;
    private String engineerName;
    private String engineerEmail;
    private List<Long> projectIds;
    private List<IssueResponse> assignedIssues;
}
