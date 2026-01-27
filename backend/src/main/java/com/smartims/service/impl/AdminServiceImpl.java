package com.smartims.service.impl;

import com.smartims.dto.AdminOverviewResponse;
import com.smartims.dto.EngineerWorkloadResponse;
import com.smartims.entity.Issue;
import com.smartims.entity.User;
import com.smartims.enums.IssueStatus;
import com.smartims.repository.IssueRepository;
import com.smartims.repository.UserRepository;
import com.smartims.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    @Override
    public AdminOverviewResponse getOverview() {
        AdminOverviewResponse response = new AdminOverviewResponse();

        response.setTotalIssues(issueRepository.count());
        response.setOpenIssues(issueRepository.countByStatus(IssueStatus.OPEN));
        response.setInProgressIssues(issueRepository.countByStatus(IssueStatus.IN_PROGRESS));
        response.setClosedIssues(issueRepository.countByStatus(IssueStatus.CLOSED));
        response.setSlaBreached(0); // SLA will be added later

        return response;
    }

    @Override
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    @Override
    public void updatePriority(Long issueId, String priority) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        issue.setPriorityLevel(priority);
        issueRepository.save(issue);
    }

    @Override
    public void assignEngineer(Long issueId, Long engineerId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        issue.setAssignedEngineer(engineer);
        issueRepository.save(issue);
    }

    @Override
    public List<EngineerWorkloadResponse> getEngineerWorkload() {
        // Will be implemented after EngineerController
        return new ArrayList<>();
    }
}
