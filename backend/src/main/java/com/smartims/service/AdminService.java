package com.smartims.service;

import com.smartims.dto.AdminOverviewResponse;
import com.smartims.dto.EngineerWorkloadResponse;
import com.smartims.entity.Issue;

import java.util.List;

public interface AdminService {
    AdminOverviewResponse getOverview();
    List<Issue> getAllIssues();
    void updatePriority(Long issueId, String priority);
    void assignEngineer(Long issueId, Long engineerId);
    List<EngineerWorkloadResponse> getEngineerWorkload();
}
