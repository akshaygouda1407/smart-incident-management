package com.smartims.service;

import com.smartims.dto.EngineerDashboardResponse;
import com.smartims.entity.Issue;

import java.util.List;

public interface EngineerService {

    EngineerDashboardResponse getDashboard(String engineerEmail);

    List<Issue> getMyIssues(String engineerEmail);

    void updateIssueStatus(Long issueId, String engineerEmail);
}

