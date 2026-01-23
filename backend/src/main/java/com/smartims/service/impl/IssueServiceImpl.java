package com.smartims.service.impl;

import com.smartims.dto.CreateIssueRequest;
import com.smartims.entity.Issue;
import com.smartims.enums.IssueStatus;
import com.smartims.repository.IssueRepository;
import com.smartims.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;

    @Override
    public void createIssue(CreateIssueRequest request, String createdBy) {

        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(IssueStatus.OPEN)
                .createdBy(createdBy)
                .build();

        issueRepository.save(issue);
    }
}
