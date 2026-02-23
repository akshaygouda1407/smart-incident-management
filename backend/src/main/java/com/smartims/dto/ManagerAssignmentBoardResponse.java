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
public class ManagerAssignmentBoardResponse {
    private List<ManagerEngineerAssignmentResponse> engineers;
    private List<IssueResponse> unassignedIssues;
}
