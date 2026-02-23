package com.smartims.service;

import com.smartims.dto.CreateIssueRequest;
import com.smartims.dto.IssueResponse;
import com.smartims.dto.ManagerAssignmentBoardResponse;
import com.smartims.dto.SlaComplianceResponse;
import com.smartims.dto.SlaStatusResponse;
import com.smartims.entity.Issue;
import com.smartims.enums.IssueStatus;
import com.smartims.enums.Severity;

import java.util.List;

public interface IssueService {

    long countByStatus(IssueStatus status);

    IssueResponse createIssue(CreateIssueRequest request, String createdBy);

    List<Issue> getIssuesByProject(Long projectId);


    void updateIssueStatus(
            Long issueId,
            IssueStatus newStatus,
            String role
    );

    void assignEngineer(Long issueId, Long engineerId);

    void autoAssignEngineer(Long issueId);

    SlaStatusResponse getSlaStatus(Long issueId);

    SlaComplianceResponse getSlaComplianceSummary();

    IssueResponse getIssueById(Long id);

    List<IssueResponse> getAllIssues();

    IssueResponse updateIssueDetails(Long issueId, String title, String description, Severity severity);

    IssueResponse updateIssueSeverity(Long issueId, Severity severity);

    IssueResponse assignIssue(Long issueId, Long engineerId);

    List<IssueResponse> getIssuesByEngineer(Long engineerId);

    ManagerAssignmentBoardResponse getManagerAssignmentBoard();

    ManagerAssignmentBoardResponse autoAssignUnassignedIssuesForManager();

}
