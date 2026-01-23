package com.smartims.service;

import com.smartims.dto.CreateIssueRequest;

public interface IssueService {

    void createIssue(CreateIssueRequest request, String createdBy);
}
