package com.smartims.service;

import com.smartims.dto.CreateIssueRequest;
import com.smartims.enums.IssueStatus;

public interface IssueService {

    long countByStatus(IssueStatus status);

    void createIssue(CreateIssueRequest request, String createdBy);

    void updateIssueStatus(
            Long issueId,
            IssueStatus newStatus,
            String role
    );
}
