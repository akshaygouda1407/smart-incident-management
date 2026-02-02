package com.smartims.service;

import com.smartims.dto.IssueActivityResponse;
import com.smartims.entity.Issue;

import java.util.List;

public interface IssueActivityService {

    void logActivity(
            Issue issue,
            String action,
            String description
    );

    List<IssueActivityResponse> getTimeline(Long issueId);
}
